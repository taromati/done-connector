package me.taromati.doneconnector.afreecatv;

import me.taromati.doneconnector.exception.DoneException;
import me.taromati.doneconnector.exception.ExceptionCode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AfreecaTVApi {
    public static AfreecaTVLiveInfo getPlayerLive(String bjid) {
        String requestURL = "https://live.afreecatv.com/afreeca/player_live_api.php?bjid=" + bjid;

        try (HttpClient client = HttpClient.newHttpClient()) {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("bid", bjid);
//            bodyJson.put("bno", "264764410");
            bodyJson.put("type", "live");
            bodyJson.put("confirm_adult", "false");
            bodyJson.put("player_type", "html5");
            bodyJson.put("mode", "landing");
            bodyJson.put("from_api", "0");
            bodyJson.put("pwd", "");
            bodyJson.put("stream_type", "common");
            bodyJson.put("quality", "HD");

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(ofFormData(bodyJson))
                    .uri(URI.create(requestURL))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build(); // HttpRequest 생성


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(response.body());
                JSONObject channel = (JSONObject) jsonObject.get("CHANNEL");
                return new AfreecaTVLiveInfo(
                        channel.get("CHDOMAIN").toString(),
                        channel.get("CHATNO").toString(),
                        channel.get("FTK").toString(),
                        channel.get("TITLE").toString(),
                        channel.get("BJID").toString(),
                        channel.get("BNO").toString(),
                        String.valueOf(Integer.parseInt(channel.get("CHPT").toString()) + 1)
                );
            } else {
                throw new DoneException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
            }
        } catch (Exception e) {
            throw new DoneException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
        }
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
