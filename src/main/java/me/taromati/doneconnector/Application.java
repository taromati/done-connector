//package me.taromati.doneconnector;
//
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
//public class Application {
//    public static void main(String[] args) throws InterruptedException {
//        String id = "SOOP_USER";
//        Map<String, String> user = new HashMap<>();
//        DoneConnector.debug = true;
//
//        user.put("id", id);
//        user.put("nickname", "닉네임");
//        user.put("tag", "마크닉네임");
//
//        SoopLiveInfo liveInfo = SoopApi.getPlayerLive(id);
//        Draft_6455 draft6455 = new Draft_6455(
//                Collections.emptyList(),
//                Collections.singletonList(new Protocol("chat"))
//        );
//        SoopWebSocket webSocket = new SoopWebSocket(String.format("wss://%s:%s/Websocket/%s", liveInfo.CHDOMAIN().toLowerCase(), liveInfo.CHPT(), liveInfo.BJID()), draft6455, liveInfo, user, new HashMap<>());
//
//        webSocket.connectBlocking(10, TimeUnit.SECONDS);
//
//        while (true) {
//            Thread.sleep(1000);
//        }
//    }
//}
