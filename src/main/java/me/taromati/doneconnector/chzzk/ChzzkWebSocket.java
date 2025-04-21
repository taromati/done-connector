package me.taromati.doneconnector.chzzk;

import lombok.Getter;
import me.taromati.doneconnector.DoneConnector;
import me.taromati.doneconnector.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ChzzkWebSocket extends WebSocketClient {
    private final String chatChannelId;
    private final String accessToken;
    private final String extraToken;
    @Getter
    private final Map<String, String> chzzkUser;
    private final HashMap<Integer, List<String>> donationRewards;
    private final Logger logger;

    private Thread pingThread;

    private boolean isAlive = true;

    private static final int CHZZK_CHAT_CMD_PING = 0;
    private static final int CHZZK_CHAT_CMD_PONG = 10000;
    private static final int CHZZK_CHAT_CMD_CONNECT = 100;
    private static final int CHZZK_CHAT_CMD_CONNECT_RES = 10100;

    private static final int CHZZK_CHAT_CMD_SEND_CHAT = 3101;
    private static final int CHZZK_CHAT_CMD_REQUEST_RECENT_CHAT = 5101;
    private static final int CHZZK_CHAT_CMD_CHAT = 93101;
    private static final int CHZZK_CHAT_CMD_DONATION = 93102;

    private final JSONParser parser = new JSONParser();

    public ChzzkWebSocket(String serverUri, String chatChannelId, String accessToken, String extraToken, Map<String, String> chzzkUser, HashMap<Integer, List<String>> donationRewards, Logger logger) {
        super(URI.create(serverUri));
        this.setConnectionLostTimeout(0);

        this.chatChannelId = chatChannelId;
        this.accessToken = accessToken;
        this.extraToken = extraToken;
        this.chzzkUser = chzzkUser;
        this.donationRewards = donationRewards;
        this.logger = logger;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.done("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 웹소켓 연결이 연결되었습니다.");

        // Connect msg Send
        JSONObject baseObject = new JSONObject();
        baseObject.put("ver", "2");
        baseObject.put("svcid", "game");
        baseObject.put("cid", this.chatChannelId);

        JSONObject sendObject = new JSONObject(baseObject);
        sendObject.put("cmd", CHZZK_CHAT_CMD_CONNECT);
        sendObject.put("tid", 1);

        JSONObject bdyObject = new JSONObject();
        bdyObject.put("uid", null);
        bdyObject.put("devType", 2001);
        bdyObject.put("accTkn", this.accessToken);
        bdyObject.put("auth", "READ");

        sendObject.put("bdy", bdyObject);

        send(sendObject.toJSONString());

        pingThread = new Thread(() -> {
            while (isAlive) {
                try {
                    Thread.sleep(19996);

                    JSONObject pongObject = new JSONObject();
                    pongObject.put("cmd", CHZZK_CHAT_CMD_PONG);
                    pongObject.put("ver", 2);
                    send(pongObject.toJSONString());
                } catch (InterruptedException ignore) {
//                    logger.info(ChatColor.RED + "치지직 웹소켓 핑 스레드가 종료되었습니다.");
                }
            }
        });
        pingThread.start();
    }

    @Override
    public void onMessage(String message) {
        logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] onMessage: " + message);

        try {
            JSONObject messageObject = (JSONObject) parser.parse(message);

            logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 파싱 시작");

            int cmd = Integer.parseInt(messageObject.get("cmd").toString());
            if (cmd == CHZZK_CHAT_CMD_PING) {
                JSONObject pongObject = new JSONObject();
                pongObject.put("cmd", CHZZK_CHAT_CMD_PONG);
                pongObject.put("ver", 2);
                send(pongObject.toJSONString());
                logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] ping");

                return;
            }

            if (cmd == CHZZK_CHAT_CMD_PONG) {
                logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] pong");

                return;
            }

            JSONObject bdyObject = (JSONObject) ((JSONArray) messageObject.get("bdy")).get(0);
            String uid = (String) bdyObject.get("uid");
            String msg = (String) bdyObject.get("msg");
            String nickname = "익명";

            if (Objects.equals(uid, "anonymous") == false) {
                String profile = (String) bdyObject.get("profile");
                JSONObject profileObejct = (JSONObject) parser.parse(profile);
                nickname = (String) profileObejct.get("nickname");
            }

            String extras = (String) bdyObject.get("extras");
            JSONObject extraObject = (JSONObject) parser.parse(extras);

            if (extraObject.get("payAmount") == null) {
                logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 구독 메시지 무시");

                return;
            }

            int payAmount = Integer.parseInt(extraObject.get("payAmount").toString());

            logger.debug("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 파싱 완료");
            logger.info(ChatColor.YELLOW + nickname + ChatColor.WHITE + "님께서 " + ChatColor.GREEN + payAmount + "원" + ChatColor.WHITE + "을 후원해주셨습니다.");

            List<String> commands = null;

            if (donationRewards.containsKey(payAmount)) {
                commands = donationRewards.get(payAmount);
            } else {
                commands = donationRewards.get(0);
            }

            if (commands == null) {
                return;
            }

            if (DoneConnector.random) {
                Random rand = new Random();
                int randomIndex = rand.nextInt(commands.size());
                String command = commands.get(randomIndex);
                call(chzzkUser.get("tag"), nickname, payAmount, msg, command);
            } else {
                for (String command : commands) {
                    call(chzzkUser.get("tag"), nickname, payAmount, msg, command);
                }
            }

        } catch (Exception e) {
            logger.error("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 메시지 파싱 중 오류가 발생했습니다.");
            logger.debug(e.toString());
        }
    }

    private void call(String tag, String nickname, int payAmount, String msg, String command) {
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
                        .callSyncMethod(DoneConnector.plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        System.out.println("onClose: " + code + ", " + reason + ", " + remote);
        logger.error("[ChzzkWebsocket][" + chzzkUser.get("nickname") + "] 치지직 웹소켓 연결이 끊겼습니다.");

        isAlive = false;

        pingThread.interrupt();
        pingThread = null;
    }

    @Override
    public void onError(Exception ex) {
        logger.error("onError: " + ex.getMessage());
    }
}
