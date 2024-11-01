package me.taromati.doneconnector;

import me.taromati.doneconnector.afreecatv.AfreecaTVApi;
import me.taromati.doneconnector.afreecatv.AfreecaTVLiveInfo;
import me.taromati.doneconnector.afreecatv.AfreecaTVWebSocket;
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
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.Protocol;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DoneConnector extends JavaPlugin implements Listener {
    public static Plugin plugin;

    public static boolean debug;
    public static boolean random;

    private static final List<Map<String, String>> chzzkUserList = new ArrayList<>();
    private static final List<Map<String, String>> afreecaTVUserList = new ArrayList<>();
    private static final HashMap<Integer, List<String>> donationRewards = new HashMap<>();
    List<ChzzkWebSocket> chzzkWebSocketList = new ArrayList<>();
    List<AfreecaTVWebSocket> afreecaTVWebSocketList = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("done")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("done")).setTabCompleter(this);

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
            connectChzzkList();
        } catch (InterruptedException e) {
            Logger.info(ChatColor.RED + "치지직 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            connectAfreecaTVList();
        } catch (InterruptedException e) {
            Logger.info(ChatColor.RED + "아프리카TV 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Logger.info(ChatColor.GREEN + "플러그인 활성화 완료.");
    }

    @Override
    public void onDisable() {
        try {
            disconnectChzzkList();
            disconnectAfreecaTVList();
        } catch (InterruptedException e) {
            Logger.info(ChatColor.RED + "플러그인 비활성화 중 오류가 발생했습니다.");
        }
        Logger.info(ChatColor.GREEN + "플러그인 비활성화 완료.");
    }

    private void clearConfig() {
        debug = false;
        random = false;
        chzzkUserList.clear();
        afreecaTVUserList.clear();
        donationRewards.clear();
        reloadConfig();
    }

    private void loadConfig() throws DoneException {
        this.saveResource("config.yml", false);
        try {
            debug = this.getConfig().getBoolean("디버그");
            random = this.getConfig().getBoolean("랜덤 보상");
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

                Map<String, String> userMap = new HashMap<>();
                userMap.put("nickname", nickname);
                userMap.put("id", id);
                userMap.put("tag", tag);
                chzzkUserList.add(userMap);
                if(debug) {
                    Logger.info(ChatColor.WHITE + "치지직 유저: " + userMap.toString());
                }
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }
        Logger.info(ChatColor.GREEN + "치지직 아이디 " + chzzkUserList.size() + "개 로드 완료.");

        try {
            if (debug) {
                Logger.info(ChatColor.WHITE + "아프리카 아이디 로드 중...");
                Logger.info(this.getConfig().getConfigurationSection("아프리카").getKeys(false).toString());
            }
            for (String nickname : this.getConfig().getConfigurationSection("아프리카").getKeys(false)) {
                if (debug) {
                    Logger.info(ChatColor.WHITE + "아프리카 닉네임: " + nickname);
                }
                String id = this.getConfig().getString("아프리카." + nickname + ".식별자");
                if (debug) {
                    Logger.info(ChatColor.WHITE + "아프리카 아이디: " + id);
                }
                String tag = this.getConfig().getString("아프리카." + nickname + ".마크닉네임");
                if (debug) {
                    Logger.info(ChatColor.WHITE + "아프리카 마크닉네임: " + tag);
                }
                if (id == null || tag == null) {
                    throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                }

                Map<String, String> userMap = new HashMap();
                userMap.put("nickname", nickname);
                userMap.put("id", id);
                userMap.put("tag", tag);
                afreecaTVUserList.add(userMap);
                if(debug) {
                    Logger.info(ChatColor.WHITE + "아프리카 유저: " + userMap.toString());
                }
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }
        Logger.info(ChatColor.GREEN + "아프리카 아이디 " + afreecaTVUserList.size() + "개 로드 완료.");

        if (chzzkUserList.isEmpty() && afreecaTVUserList.isEmpty()) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

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

    private String disconnectByNickName(String nickname) {
        for (ChzzkWebSocket webSocket : chzzkWebSocketList) {
            if (Objects.equals(webSocket.getChzzkUser().get("nickname"), nickname)) {
                webSocket.close();
                chzzkWebSocketList.remove(webSocket);
                return "chzzk";
            }
        }
        for (AfreecaTVWebSocket webSocket : afreecaTVWebSocketList) {
            if (Objects.equals(webSocket.getAfreecaTVUser().get("nickname"), nickname)) {
                webSocket.close();
                afreecaTVWebSocketList.remove(webSocket);
                return "afreecaTV";
            }
        }
        return null;
    }

    private void connectChzzk(Map<String, String> chzzkUser) throws InterruptedException {
        try {
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
            webSocket.connect();
            chzzkWebSocketList.add(webSocket);
        } catch (Exception e) {
            Logger.info(ChatColor.RED + "[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 채팅에 연결 중 오류가 발생했습니다.");
            if (debug) {
                Logger.info(ChatColor.LIGHT_PURPLE + e.getMessage());
            }
        }
    }

    private void connectChzzkList() throws InterruptedException {
        for (Map<String, String> chzzkUser : chzzkUserList) {
            connectChzzk(chzzkUser);
        }
    }

    private void disconnectChzzkList() throws InterruptedException {
        for (ChzzkWebSocket webSocket : chzzkWebSocketList) {
            webSocket.close();
        }
        chzzkWebSocketList.clear();
    }

    private void connectAfreecaTV(Map<String, String> afreecaTVUser) throws InterruptedException {
        String afreecaTVId = afreecaTVUser.get("id");
        try {
            AfreecaTVLiveInfo liveInfo = AfreecaTVApi.getPlayerLive(afreecaTVId);
            Draft_6455 draft6455 = new Draft_6455(
                    Collections.emptyList(),
                    Collections.singletonList(new Protocol("chat"))
            );
            AfreecaTVWebSocket webSocket = new AfreecaTVWebSocket("wss://" + liveInfo.CHDOMAIN() + ":" + liveInfo.CHPT() + "/Websocket/" + liveInfo.BJID(), draft6455, liveInfo, afreecaTVUser, donationRewards);
            webSocket.connect();
            afreecaTVWebSocketList.add(webSocket);
        } catch (Exception e) {
            Logger.info(ChatColor.RED + "[AfreecaTVWebsocket][" + afreecaTVUser.get("nickname") + "] 아프리카TV 채팅에 연결 중 오류가 발생했습니다.");
            if (debug) {
                Logger.info(ChatColor.LIGHT_PURPLE + e.getMessage());
            }
        }
    }

    private void connectAfreecaTVList() throws InterruptedException {
        for (Map<String, String> afreecaTVUser : afreecaTVUserList) {
            connectAfreecaTV(afreecaTVUser);
        }
    }

    private void disconnectAfreecaTVList() throws InterruptedException {
        for (AfreecaTVWebSocket webSocket : afreecaTVWebSocketList) {
            webSocket.close();
        }
        afreecaTVWebSocketList.clear();
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
                    Logger.info(ChatColor.YELLOW + "후원 기능을 활성화 합니다.");
                    connectChzzkList();
                    connectAfreecaTVList();
                } else if (args[0].equalsIgnoreCase("off")) {
                    Logger.info(ChatColor.YELLOW + "후원 기능을 비활성화 합니다.");
                    disconnectChzzkList();
                    disconnectAfreecaTVList();
                } else if (args[0].equalsIgnoreCase("reconnect")) {
                    Logger.info(ChatColor.YELLOW + "후원 기능을 재접속합니다.");
                    Logger.say(ChatColor.YELLOW + "후원 기능을 재접속합니다.");

                    if (args.length < 2) {
                        Logger.info(ChatColor.YELLOW + "all 혹은 스트리머 닉네임을 입력해주세요.");
                        return false;
                    }

                    String target = args[1];
                    if (Objects.equals(target, "all")) {
                        disconnectChzzkList();
                        connectChzzkList();
                        disconnectAfreecaTVList();
                        connectAfreecaTVList();
                        return true;
                    }

                    String platform = disconnectByNickName(target);
                    if (Objects.equals(platform, "chzzk")) {
                        Map<String, String> chzzkUser = chzzkUserList.stream()
                                .filter(user -> Objects.equals(user.get("nickname"), target))
                                .toList()
                                .getFirst();
                        if (chzzkUser == null) {
                            return false;
                        }
                        connectChzzk(chzzkUser);
                    } else if (Objects.equals(platform, "afreecaTV")) {
                        Map<String, String> afreecaTVUser = afreecaTVUserList.stream()
                                .filter(user -> Objects.equals(user.get("nickname"), target))
                                .toList()
                                .getFirst();
                        if (afreecaTVUser == null) {
                            return false;
                        }
                        connectAfreecaTV(afreecaTVUser);
                    } else {
                        Logger.info(ChatColor.YELLOW + "닉네임을 찾을 수 없습니다.");
                        return false;
                    }
                    Logger.info(ChatColor.GREEN + "[" + target + "] 재 접속을 완료 했습니다.");
                } else if (args[0].equalsIgnoreCase("reload")) {
                    Logger.info(ChatColor.YELLOW + "후원 설정을 다시 불러옵니다.");
                    Logger.say(ChatColor.YELLOW + "후원 설정을 다시 불러옵니다.");
                    disconnectChzzkList();
                    disconnectAfreecaTVList();
                    clearConfig();
                    loadConfig();
                    connectChzzkList();
                    connectAfreecaTVList();
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

    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.isOp() == false) {
            return Collections.emptyList();
        } else if (args.length == 1) {
            List<String> commandList = new ArrayList<>(Arrays.asList("on", "off", "reconnect", "reload"));

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
