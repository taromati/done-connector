//package me.taromati.doneconnector;
//
//import me.taromati.doneconnector.afreecatv.AfreecaTVApi;
//import me.taromati.doneconnector.afreecatv.AfreecaTVLiveInfo;
//import me.taromati.doneconnector.afreecatv.AfreecaTVWebSocket;
//import org.java_websocket.drafts.Draft_6455;
//import org.java_websocket.extensions.IExtension;
//import org.java_websocket.protocols.IProtocol;
//import org.java_websocket.protocols.Protocol;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//public class Applicaton {
//    public static void main(String[] args) throws InterruptedException {
//        String id = "madaomm";
//        Map<String, String> user = new HashMap<>();
//        user.put("id", id);
//        user.put("nickname", "닉네임");
//        user.put("tag", "마크닉네임");
//        AfreecaTVLiveInfo liveInfo = AfreecaTVApi.getPlayerLive(id);
//
//        Draft_6455 draft6455 = new Draft_6455(
//                Collections.emptyList(),
//                Collections.singletonList(new Protocol("chat"))
//        );
//
//        AfreecaTVWebSocket webSocket = new AfreecaTVWebSocket("wss://" + liveInfo.CHDOMAIN().toLowerCase() + ":" + liveInfo.CHPT() + "/Websocket/" + liveInfo.BJID(), draft6455, liveInfo, user, new HashMap<>());
//        webSocket.connectBlocking(10, TimeUnit.SECONDS);
//        while (true) {
//            Thread.sleep(1000);
//        }
//    }
//}
