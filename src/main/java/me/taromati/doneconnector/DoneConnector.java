package me.taromati.doneconnector;

import me.taromati.doneconnector.logger.Logger;
import me.taromati.doneconnector.logger.LoggerFactory;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.Protocol;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class DoneConnector extends JavaPlugin implements Listener, DonationListener {
    public static Plugin plugin;

    public static boolean debug;
    public static boolean random;
    public static boolean poong = false;

    private Logger logger;

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
            logger.error("설정 파일을 불러오는 중 오류가 발생했습니다.");
            logger.debug(e.getMessage());
            logger.error("플러그인을 종료합니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        try {
            connectChzzkList();
        } catch (InterruptedException e) {
            logger.error("치지직 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        try {
            connectSoopList();
        } catch (InterruptedException e) {
            logger.error("숲 채팅에 연결 중 오류가 발생했습니다.");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }

        logger.done("플러그인 활성화 완료.");
    }

    @Override
    public void onDisable() {
        try {
            disconnectChzzkList();
            disconnectSoopList();
        } catch (InterruptedException e) {
            logger.error("플러그인 비활성화 중 오류가 발생했습니다.");
        }

        logger.done("플러그인 비활성화 완료.");
    }

    @Override
    public void onDonation(String platform, String streamerTag, String donorNickname, int amount, String message) {
        logger.info(ChatColor.YELLOW + donorNickname + ChatColor.WHITE + "님께서 " + ChatColor.GREEN + amount + "원" + ChatColor.WHITE + "을 후원해주셨습니다.");

        List<String> commands = null;

        if (donationRewards.containsKey(amount)) {
            commands = donationRewards.get(amount);
        } else {
            commands = donationRewards.get(0);
        }

        if (commands == null) {
            return;
        }

        if (random) {
            Random rand = new Random();
            int randomIndex = rand.nextInt(commands.size());
            String command = commands.get(randomIndex);
            executeCommand(streamerTag, donorNickname, amount, message, command);
        } else {
            for (String command : commands) {
                executeCommand(streamerTag, donorNickname, amount, message, command);
            }
        }
    }

    private void executeCommand(String tag, String nickname, int payAmount, String msg, String command) {
        String[] commandArray = command.split(";");

        for (String cmd : commandArray) {
            String tempCommand = cmd;
            tempCommand = tempCommand.replaceAll("%tag%", tag);
            tempCommand = tempCommand.replaceAll("%name%", nickname);
            tempCommand = tempCommand.replaceAll("%amount%", String.valueOf(payAmount));
            tempCommand = tempCommand.replaceAll("%message%", msg);
            String finalCommand = tempCommand;

            try {
                Bukkit.getScheduler()
                        .callSyncMethod(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.toString());
            }
        }
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

        FileConfiguration config = this.getConfig();

        try {
            debug = config.getBoolean("디버그");
            random = config.getBoolean("랜덤 보상");
            if (config.contains("숲풍선갯수로출력")) {
                poong = config.getBoolean("숲풍선갯수로출력");
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.CONFIG_LOAD_ERROR);
        }

        logger = LoggerFactory.createBukkitLogger(debug);

        try {
            logger.info("치지직 아이디 로드 중...");
            Set<String> nicknameList = Objects.requireNonNull(config.getConfigurationSection("치지직"))
                    .getKeys(false);
            logger.debug(nicknameList.toString());

            for (String nickname : nicknameList) {
                logger.debug("치지직 닉네임: " + nickname);
                String id = config.getString("치지직." + nickname + ".식별자");
                logger.debug("치지직 아이디: " + id);
                String tag = config.getString("치지직." + nickname + ".마크닉네임");
                logger.debug("치지직 마크닉네임: " + tag);

                if (id == null || tag == null) {
                    throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                }

                Map<String, String> userMap = new HashMap<>();
                userMap.put("nickname", nickname);
                userMap.put("id", id);
                userMap.put("tag", tag);
                chzzkUserList.add(userMap);

                logger.debug("치지직 유저: " + userMap);
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

        logger.done("치지직 아이디 " + chzzkUserList.size() + "개 로드 완료.");

        try {
            logger.debug("숲 아이디 로드 중...");
            Set<String> nicknameList = new HashSet<>();
            if (this.getConfig().getConfigurationSection("숲") != null) {
                nicknameList.addAll(this.getConfig().getConfigurationSection("숲").getKeys(false));
            }
            if (this.getConfig().getConfigurationSection("아프리카") != null) {
                nicknameList.addAll(this.getConfig().getConfigurationSection("아프리카").getKeys(false));
            }
            logger.debug(nicknameList.toString());

            for (String nickname : nicknameList) {
                logger.debug("숲 닉네임: " + nickname);
                if (config.getString("숲." + nickname + ".식별자") != null) {
                    String id = config.getString("숲." + nickname + ".식별자");
                    logger.debug("숲 아이디: " + id);
                    String tag = config.getString("숲." + nickname + ".마크닉네임");
                    logger.debug("숲 마크닉네임: " + tag);

                    if (id == null || tag == null) {
                        throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                    }

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("nickname", nickname);
                    userMap.put("id", id);
                    userMap.put("tag", tag);
                    soopUserList.add(userMap);
                    logger.debug("숲 유저: " + userMap);
                } else if (config.getString("아프리카." + nickname + ".식별자") != null) {
                    // TODO: 하위호환용, 추후 제거.
                    String id = config.getString("아프리카." + nickname + ".식별자");
                    logger.debug("아프리카 아이디: " + id);
                    String tag = config.getString("아프리카." + nickname + ".마크닉네임");
                    logger.debug("아프리카 마크닉네임: " + tag);

                    if (id == null || tag == null) {
                        throw new DoneException(ExceptionCode.ID_NOT_FOUND);
                    }

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("nickname", nickname);
                    userMap.put("id", id);
                    userMap.put("tag", tag);
                    soopUserList.add(userMap);
                    logger.debug("아프리카 유저: " + userMap);
                }

            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

        logger.done("숲 아이디 " + soopUserList.size() + "개 로드 완료.");

        if (chzzkUserList.isEmpty() && soopUserList.isEmpty()) {
            throw new DoneException(ExceptionCode.ID_NOT_FOUND);
        }

        try {
            for (String price : Objects.requireNonNull(config.getConfigurationSection("후원 보상")).getKeys(false)) {
                donationRewards.put(Integer.valueOf(price), this.getConfig().getStringList("후원 보상." + price));
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.REWARD_PARSE_ERROR);
        }

        if (donationRewards.keySet().isEmpty()) {
            throw new DoneException(ExceptionCode.REWARD_NOT_FOUND);
        }

        logger.done("후원 보상 목록 " + donationRewards.keySet().size() + "개 로드 완료.");
    }

    private void disconnectByNickName(String target) {
        chzzkWebSocketList = chzzkWebSocketList.stream()
                .filter(chzzkWebSocket -> {
                    if (Objects.equals(chzzkWebSocket.getChzzkUser().get("nickname"), target) || Objects.equals(chzzkWebSocket.getChzzkUser().get("tag"), target)) {
                        chzzkWebSocket.close();
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        soopWebSocketList = soopWebSocketList.stream()
                .filter(soopWebSocket -> {
                    if (Objects.equals(soopWebSocket.getSoopUser().get("nickname"), target) || Objects.equals(soopWebSocket.getSoopUser().get("tag"), target)) {
                        soopWebSocket.close();
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private boolean connectChzzk(Map<String, String> chzzkUser) {
        try {
            String chzzkId = chzzkUser.get("id");
            String chatChannelId = ChzzkApi.getChatChannelId(chzzkId);

            logger.debug("채널 아이디 조회 완료: " + chatChannelId);

            String token = ChzzkApi.getAccessToken(chatChannelId);
            String accessToken = token.split(";")[0];
            String extraToken = token.split(";")[1];

            logger.debug("액세스 토큰 조회 완료: " + accessToken + ", " + extraToken);

            ChzzkWebSocket webSocket = new ChzzkWebSocket("wss://kr-ss1.chat.naver.com/chat", chatChannelId, accessToken, extraToken, chzzkUser, logger, this);
            webSocket.connect();
            chzzkWebSocketList.add(webSocket);
            return true;
        } catch (Exception e) {
            logger.error("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 채팅에 연결 중 오류가 발생했습니다.");
            logger.debug(e.getMessage());
            return false;
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

    private boolean connectSoop(Map<String, String> soopUser) throws InterruptedException {
        String soopId = soopUser.get("id");

        try {
            SoopLiveInfo liveInfo = SoopApi.getPlayerLive(soopId, logger);
            Draft_6455 draft6455 = new Draft_6455(
                    Collections.emptyList(),
                    Collections.singletonList(new Protocol("chat"))
            );
            SoopWebSocket webSocket = new SoopWebSocket("wss://" + liveInfo.CHDOMAIN() + ":" + liveInfo.CHPT() + "/Websocket/" + liveInfo.BJID(), draft6455, liveInfo, soopUser, poong, logger, this);
            webSocket.connect();
            soopWebSocketList.add(webSocket);
            return true;
        } catch (Exception e) {
            logger.error("[SoopWebsocket][" + soopUser.get("nickname") + "] 숲 채팅에 연결 중 오류가 발생했습니다.");
            logger.debug(e.getMessage());
            return false;
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
            return false;
        } else if (sender.isOp() == false) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return false;
        } else if (args.length < 1) {
            return false;
        }

        try {
            String cmd = args[0];

            if (cmd.equalsIgnoreCase("on")) {
                logger.warn("후원 기능을 활성화 합니다.");
                connectChzzkList();
                connectSoopList();
            } else if (cmd.equalsIgnoreCase("off")) {
                logger.warn("후원 기능을 비활성화 합니다.");
                disconnectChzzkList();
                disconnectSoopList();
            } else if (cmd.equalsIgnoreCase("reconnect")) {
                logger.warn("후원 기능을 재접속합니다.");

                if (args.length < 2) {
                    logger.warn("all 혹은 스트리머 닉네임을 입력해주세요.");
                    return false;
                }

                String target = args[1];

                if (Objects.equals(target, "all")) {
                    disconnectChzzkList();
                    disconnectSoopList();
                    connectChzzkList();
                    connectSoopList();
                    logger.done("후원 기능 재 접속을 완료 했습니다.");

                    return true;
                }
                // 방송/마크 닉네임으로 재접속
                {
                    disconnectByNickName(target);
                    int reconnectCount = chzzkUserList.stream()
                            .filter(user -> Objects.equals(user.get("nickname"), target) || Objects.equals(user.get("tag"), target))
                            .map(user -> {
                                try {
                                    connectChzzk(user);
                                    logger.done("[" + target + "] 재 접속을 완료 했습니다.");
                                    return 1;
                                } catch (Exception e) {
                                    logger.error("[" + target + "] 채팅에 연결 중 오류가 발생했습니다.");
                                }
                                return 0;
                            })
                            .reduce(Integer::sum)
                            .orElse(0);

                    reconnectCount += soopUserList.stream()
                            .filter(user -> Objects.equals(user.get("nickname"), target) || Objects.equals(user.get("tag"), target))
                            .map(user -> {
                                try {
                                    connectSoop(user);
                                    logger.done("[" + target + "] 재 접속을 완료 했습니다.");
                                    return 1;
                                } catch (Exception e) {
                                    logger.error("[" + target + "] 채팅에 연결 중 오류가 발생했습니다.");
                                }
                                return 0;
                            })
                            .reduce(Integer::sum)
                            .orElse(0);

                    if (reconnectCount <= 0) {
                        logger.warn("닉네임을 찾을 수 없습니다.");
                        return false;
                    }
                }
            } else if (cmd.equalsIgnoreCase("add")) {
                if (args.length < 5) {
                    logger.error("옵션 누락. /done add <플랫폼> <방송닉> <방송ID> <마크닉>");
                    return false;
                }
                String platform = args[1];
                String nickname = args[2];
                String id = args[3];
                String tag = args[4];

                switch (platform) {
                    case "치지직" -> {
                        Map<String, String> userMap = new HashMap<>();
                        userMap.put("nickname", nickname);
                        userMap.put("id", id);
                        userMap.put("tag", tag);

                        if (connectChzzk(userMap)) {
                            chzzkUserList.add(userMap);
                        }
                    }
                    case "숲" -> {
                        Map<String, String> userMap = new HashMap<>();
                        userMap.put("nickname", nickname);
                        userMap.put("id", id);
                        userMap.put("tag", tag);
                        if (connectSoop(userMap)) {
                            soopUserList.add(userMap);
                        }
                    }
                }

            } else if (cmd.equalsIgnoreCase("reload")) {
                logger.warn("후원 설정을 다시 불러옵니다.");
                disconnectChzzkList();
                disconnectSoopList();
                clearConfig();
                loadConfig();
                connectChzzkList();
                connectSoopList();
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("커맨드 수행 중 오류가 발생했습니다.");

            return false;
        }

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("done") == false) {
            return Collections.emptyList();
        }

        if (sender.isOp() == false) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> commandList = new ArrayList<>(Arrays.asList("on", "off", "reconnect", "reload", "add"));

            if (args[0].isEmpty()) {
                return commandList;
            } else {
                return commandList.stream()
                        .filter((command) -> command.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reconnect")) {
            if (args[1].isEmpty()) {
                return new ArrayList<>(List.of("all"));
            } else {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        return Collections.emptyList();
    }
}
