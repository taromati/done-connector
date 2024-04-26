package me.taromati.doneconnector;

import me.taromati.doneconnector.chzzk.ChzzkApi;
import me.taromati.doneconnector.chzzk.ChzzkWebSocket;
import me.taromati.doneconnector.exception.DoneException;
import me.taromati.doneconnector.exception.ExceptionCode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class DoneConnector extends JavaPlugin implements Listener {
    public static boolean debug;
    public static Plugin plugin;

    private static final List<Map<String, String>> chzzkUserList = new ArrayList<>();
    private static final HashMap<Integer, List<String>> donationRewards = new HashMap<>();
    List<ChzzkWebSocket> chzzkWebSocketList = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("done").setExecutor(this);
        this.getCommand("done").setTabCompleter(this);

        try {
            loadConfig();
        } catch (Exception e) {
            Logger.info(ChatColor.RED + "설정 파일을 불러오는 중 오류가 발생했습니다.");
            Logger.info(ChatColor.LIGHT_PURPLE + e.getMessage());
            Logger.info(ChatColor.RED + "플러그인을 종료합니다.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            connectChzzk(chzzkUserList);
        } catch (InterruptedException e) {
            Logger.info(ChatColor.RED + "치지직 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Logger.info(ChatColor.GREEN + "플러그인 활성화 완료.");
    }

    @Override
    public void onDisable() {
        try {
            disconnectChzzk(chzzkWebSocketList);
        } catch (InterruptedException e) {
            Logger.info(ChatColor.RED + "플러그인 비활성화 중 오류가 발생했습니다.");
        }
        Logger.info(ChatColor.GREEN + "플러그인 비활성화 완료.");
    }

    private void loadConfig() throws DoneException {
        this.saveResource("config.yml", false);
        try {
            debug = this.getConfig().getBoolean("디버그");
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.CONFIG_LOAD_ERROR);
        }

        try {
            if (debug) {
                Logger.info(ChatColor.WHITE + "치지직 아이디 로드 중...");
                Logger.info(this.getConfig().getConfigurationSection("치지직").getKeys(false).toString());
            }
            for (String nickname : this.getConfig().getConfigurationSection("치지직").getKeys(false)) {
                if (debug) {
                    Logger.info(ChatColor.WHITE + "치지직 닉네임: " + nickname);
                }
                String id = this.getConfig().getString("치지직." + nickname + ".식별자");
                if (debug) {
                    Logger.info(ChatColor.WHITE + "치지직 아이디: " + id);
                }
                String tag = this.getConfig().getString("치지직." + nickname + ".마크닉네임");
                if (debug) {
                    Logger.info(ChatColor.WHITE + "치지직 마크닉네임: " + tag);
                }
                if (id == null || tag == null) {
                    throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                }

                Map<String, String> chzzkUser = new HashMap();
                chzzkUser.put("nickname", nickname);
                chzzkUser.put("id", id);
                chzzkUser.put("tag", tag);
                chzzkUserList.add(chzzkUser);
                if(debug) {
                    Logger.info(ChatColor.WHITE + "치지직 유저: " + chzzkUser.toString());
                }
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }
        if (chzzkUserList.isEmpty()) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }
        Logger.info(ChatColor.GREEN + "치지직 아이디 " + chzzkUserList.size() + "개 로드 완료.");

        try {
            for (String price : this.getConfig().getConfigurationSection("후원 보상").getKeys(false)) {
                donationRewards.put(Integer.valueOf(price), this.getConfig().getStringList("후원 보상." + price));
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.REWARD_PARSE_ERROR);
        }
        if (donationRewards.keySet().isEmpty()) {
            throw new DoneException(ExceptionCode.REWARD_NOT_FOUND);
        }
        Logger.info(ChatColor.GREEN + "후원 보상 목록 " + donationRewards.keySet().size() + "개 로드 완료.");
    }

    private void connectChzzk(List<Map<String, String>> chzzkUserList) throws InterruptedException {
        for (Map<String, String> chzzkUser : chzzkUserList) {
            String chzzkId = chzzkUser.get("id");
            String chatChannelId = ChzzkApi.getChatChannelId(chzzkId);
            if (debug) {
                Logger.info(ChatColor.GREEN + "채널 아이디 조회 완료: " + chatChannelId);
            }
            String token = ChzzkApi.getAccessToken(chatChannelId);
            String accessToken = token.split(";")[0];
            String extraToken = token.split(";")[1];
            if (debug) {
                Logger.info(ChatColor.GREEN + "액세스 토큰 조회 완료: " + accessToken + ", " + extraToken);
            }

            ChzzkWebSocket webSocket = new ChzzkWebSocket("wss://kr-ss1.chat.naver.com/chat", chatChannelId, accessToken, extraToken, chzzkUser, donationRewards);
            webSocket.connectBlocking();
            chzzkWebSocketList.add(webSocket);
        }
    }

    private void disconnectChzzk(List<ChzzkWebSocket> chzzkWebSocketList) throws InterruptedException {
        for (ChzzkWebSocket webSocket : chzzkWebSocketList) {
            webSocket.closeBlocking();
        }
        chzzkWebSocketList.clear();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("done") == false) {
            return true;
        } else if (sender.isOp() == false) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        } else if (args.length < 1) {
            return false;
        } else {
            try {
                if (args[0].equalsIgnoreCase("on")) {
                    Logger.info(ChatColor.GREEN + "후원 기능을 활성화 합니다.");
                    connectChzzk(chzzkUserList);
                } else if (args[0].equalsIgnoreCase("off")) {
                    Logger.info(ChatColor.YELLOW + "후원 기능을 비활성화 합니다.");
                    disconnectChzzk(chzzkWebSocketList);
                } else if (args[0].equalsIgnoreCase("reconnect")) {
                    Logger.info(ChatColor.YELLOW + "후원 기능을 재접속합니다.");
                    disconnectChzzk(chzzkWebSocketList);
                    connectChzzk(chzzkUserList);
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                Logger.info(ChatColor.RED + "커맨드 수행 중 오류가 발생했습니다.");
                return true;
            }

            return true;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp() == false) {
            return Collections.emptyList();
        } else if (args.length == 1) {
            List<String> commandList = new ArrayList<>(Arrays.asList("on", "off", "reconnect"));

            if (args[0].isEmpty()) {
                return commandList;
            } else {
                return commandList.stream()
                        .filter((command) -> command.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
