//package me.taromati.doneconnector;
//
//import me.taromati.doneconnector.chzzk.ChzzkApi;
//import me.taromati.doneconnector.chzzk.ChzzkWebSocket;
//import me.taromati.doneconnector.logger.LoggerFactory;
//import me.taromati.doneconnector.soop.SoopApi;
//import me.taromati.doneconnector.soop.SoopLiveInfo;
//import me.taromati.doneconnector.soop.SoopWebSocket;
//import org.java_websocket.drafts.Draft_6455;
//import org.java_websocket.protocols.Protocol;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//public class Applicaton {
//    public static void main(String[] args) throws InterruptedException {
//        // Create a simple donation listener for the standalone application
//        DonationListener listener = (platform, streamerTag, donorNickname, amount, message) -> System.out.println("[" + platform + " Donation] " + donorNickname + " donated " + amount + " KRW: " + message);
//
//        {
//            String id = "madaomm";
//            Map<String, String> user = new HashMap<>();
//            user.put("id", id);
//            user.put("nickname", "닉네임");
//            user.put("tag", "마크닉네임");
//            SoopLiveInfo liveInfo = SoopApi.getPlayerLive(id);
//
//            Draft_6455 draft6455 = new Draft_6455(
//                    Collections.emptyList(),
//                    Collections.singletonList(new Protocol("chat"))
//            );
//
//            SoopWebSocket webSocket = new SoopWebSocket(
//                    "wss://" + liveInfo.CHDOMAIN().toLowerCase() + ":" + liveInfo.CHPT() + "/Websocket/" + liveInfo.BJID(),
//                    draft6455,
//                    liveInfo,
//                    user,
//                    false,
//                    LoggerFactory.createSystemLogger(true),
//                    listener
//            );
//            webSocket.connectBlocking(10, TimeUnit.SECONDS);
//        }
//
//        {
//            String id = "45e71a76e949e16a34764deb962f9d9f";
//            Map<String, String> user = new HashMap<>();
//            user.put("id", id);
//            user.put("nickname", "닉네임");
//            user.put("tag", "마크닉네임");
//
//            // 치지직 웹소켓 연결
//            String chatChannelId = ChzzkApi.getChatChannelId(id);
//            String[] tokens = ChzzkApi.getAccessToken(chatChannelId).split(";");
//            String accessToken = tokens[0];
//            String extraToken = tokens[1];
//
//            ChzzkWebSocket webSocket = new ChzzkWebSocket(
//                    "wss://kr-ss1.chat.naver.com/chat",
//                    chatChannelId,
//                    accessToken,
//                    extraToken,
//                    user,
//                    LoggerFactory.createSystemLogger(true),
//                    listener
//            );
//            webSocket.connect();
//        }
//        while (true) {
//            Thread.sleep(1000);
//        }
//    }
//}
