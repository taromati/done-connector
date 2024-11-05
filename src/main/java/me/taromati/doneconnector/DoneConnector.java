package me.taromati.doneconnector;

import me.taromati.doneconnector.soop.SoopApi;
import me.taromati.doneconnector.soop.SoopLiveInfo;
import me.taromati.doneconnector.soop.SoopWebSocket;
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
    private static final List<Map<String, String>> soopUserList = new ArrayList<>();
    private static final HashMap<Integer, List<String>> donationRewards = new HashMap<>();
    List<ChzzkWebSocket> chzzkWebSocketList = new ArrayList<>();
    List<SoopWebSocket> soopWebSocketList = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("done")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("done")).setTabCompleter(this);

        try {
            loadConfig();
        } catch (Exception e) {
            Logger.error("설정 파일을 불러오는 중 오류가 발생했습니다.");
            Logger.debug(e.getMessage());
            Logger.error("플러그인을 종료합니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        try {
            connectChzzkList();
        } catch (InterruptedException e) {
            Logger.error("치지직 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        try {
            connectSoopList();
        } catch (InterruptedException e) {
            Logger.error("숲 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        Logger.info(ChatColor.GREEN + "플러그인 활성화 완료.");
    }

    @Override
    public void onDisable() {
        try {
            disconnectChzzkList();
            disconnectSoopList();
        } catch (InterruptedException e) {
            Logger.error("플러그인 비활성화 중 오류가 발생했습니다.");
        }

        Logger.info(ChatColor.GREEN + "플러그인 비활성화 완료.");
    }

    private void clearConfig() {
        debug = false;
        random = false;
        chzzkUserList.clear();
        soopUserList.clear();
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
            Logger.info(ChatColor.WHITE + "치지직 아이디 로드 중...");
            Set<String> nicknameList = this.getConfig().getConfigurationSection("치지직").getKeys(false);
            Logger.debug(nicknameList.toString());

            for (String nickname : nicknameList) {
                Logger.debug(ChatColor.WHITE + "치지직 닉네임: " + nickname);
                String id = this.getConfig().getString("치지직." + nickname + ".식별자");
                Logger.debug(ChatColor.WHITE + "치지직 아이디: " + id);
                String tag = this.getConfig().getString("치지직." + nickname + ".마크닉네임");
                Logger.debug(ChatColor.WHITE + "치지직 마크닉네임: " + tag);

                if (id == null || tag == null) {
                    throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                }

                Map<String, String> userMap = new HashMap<>();
                userMap.put("nickname", nickname);
                userMap.put("id", id);
                userMap.put("tag", tag);
                chzzkUserList.add(userMap);

                Logger.debug(ChatColor.WHITE + "치지직 유저: " + userMap.toString());
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

        Logger.info(ChatColor.GREEN + "치지직 아이디 " + chzzkUserList.size() + "개 로드 완료.");

        try {
            Logger.debug(ChatColor.WHITE + "숲 아이디 로드 중...");
            Set<String> nicknameList = this.getConfig().getConfigurationSection("숲").getKeys(false);
            nicknameList.addAll(this.getConfig().getConfigurationSection("아프리카").getKeys(false));
            Logger.debug(nicknameList.toString());

            for (String nickname : nicknameList) {
                Logger.debug(ChatColor.WHITE + "숲 닉네임: " + nickname);
                String id = this.getConfig().getString("숲." + nickname + ".식별자");
                Logger.debug(ChatColor.WHITE + "숲 아이디: " + id);
                String tag = this.getConfig().getString("숲." + nickname + ".마크닉네임");
                Logger.debug(ChatColor.WHITE + "숲 마크닉네임: " + tag);

                if (id == null || tag == null) {
                    throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                }

                Map<String, String> userMap = new HashMap();
                userMap.put("nickname", nickname);
                userMap.put("id", id);
                userMap.put("tag", tag);
                soopUserList.add(userMap);
                Logger.debug(ChatColor.WHITE + "숲 유저: " + userMap.toString());
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

        Logger.info(ChatColor.GREEN + "숲 아이디 " + soopUserList.size() + "개 로드 완료.");

        if (chzzkUserList.isEmpty() && soopUserList.isEmpty()) {
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
        for (SoopWebSocket webSocket : soopWebSocketList) {
            if (Objects.equals(webSocket.getSoopUser().get("nickname"), nickname)) {
                webSocket.close();
                soopWebSocketList.remove(webSocket);

                return "soop";
            }
        }
        return null;
    }

    private void connectChzzk(Map<String, String> chzzkUser) throws InterruptedException {
        try {
            String chzzkId = chzzkUser.get("id");
            String chatChannelId = ChzzkApi.getChatChannelId(chzzkId);

            Logger.debug(ChatColor.GREEN + "채널 아이디 조회 완료: " + chatChannelId);

            String token = ChzzkApi.getAccessToken(chatChannelId);
            String accessToken = token.split(";")[0];
            String extraToken = token.split(";")[1];

            Logger.debug(ChatColor.GREEN + "액세스 토큰 조회 완료: " + accessToken + ", " + extraToken);

            ChzzkWebSocket webSocket = new ChzzkWebSocket("wss://kr-ss1.chat.naver.com/chat", chatChannelId, accessToken, extraToken, chzzkUser, donationRewards);
            webSocket.connect();
            chzzkWebSocketList.add(webSocket);
        } catch (Exception e) {
            Logger.info(ChatColor.RED + "[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 채팅에 연결 중 오류가 발생했습니다.");
            Logger.debug(ChatColor.LIGHT_PURPLE + e.getMessage());
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

    private void connectSoop(Map<String, String> soopUser) throws InterruptedException {
        String soopId = soopUser.get("id");

        try {
            SoopLiveInfo liveInfo = SoopApi.getPlayerLive(soopId);
            Draft_6455 draft6455 = new Draft_6455(
                    Collections.emptyList(),
                    Collections.singletonList(new Protocol("chat"))
            );
            SoopWebSocket webSocket = new SoopWebSocket("wss://" + liveInfo.CHDOMAIN() + ":" + liveInfo.CHPT() + "/Websocket/" + liveInfo.BJID(), draft6455, liveInfo, soopUser, donationRewards);
            webSocket.connect();
            soopWebSocketList.add(webSocket);
        } catch (Exception e) {
            Logger.info(ChatColor.RED + "[SoopWebsocket][" + soopUser.get("nickname") + "] 숲 채팅에 연결 중 오류가 발생했습니다.");

            Logger.debug(ChatColor.LIGHT_PURPLE + e.getMessage());
        }
    }

    private void connectSoopList() throws InterruptedException {
        for (Map<String, String> soopUser : soopUserList) {
            connectSoop(soopUser);
        }
    }

    private void disconnectSoopList() throws InterruptedException {
        for (SoopWebSocket webSocket : soopWebSocketList) {
            webSocket.close();
        }

        soopWebSocketList.clear();
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
                    connectSoopList();
                } else if (args[0].equalsIgnoreCase("off")) {
                    Logger.info(ChatColor.YELLOW + "후원 기능을 비활성화 합니다.");
                    disconnectChzzkList();
                    disconnectSoopList();
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
                        disconnectSoopList();
                        connectSoopList();

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
                    } else if (Objects.equals(platform, "soop")) {
                        Map<String, String> soopUser = soopUserList.stream()
                                .filter(user -> Objects.equals(user.get("nickname"), target))
                                .toList()
                                .getFirst();

                        if (soopUser == null) {
                            return false;
                        }

                        connectSoop(soopUser);
                    } else {
                        Logger.warn(ChatColor.YELLOW + "닉네임을 찾을 수 없습니다.");

                        return false;
                    }

                    Logger.info(ChatColor.GREEN + "[" + target + "] 재 접속을 완료 했습니다.");
                } else if (args[0].equalsIgnoreCase("reload")) {
                    Logger.warn("후원 설정을 다시 불러옵니다.");
                    Logger.say(ChatColor.YELLOW + "후원 설정을 다시 불러옵니다.");
                    disconnectChzzkList();
                    disconnectSoopList();
                    clearConfig();
                    loadConfig();
                    connectChzzkList();
                    connectSoopList();
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                Logger.error("커맨드 수행 중 오류가 발생했습니다.");

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
