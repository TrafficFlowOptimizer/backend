package app.backend.controller.video;

import app.backend.service.VideoService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class VideoUtils {
    public static final String VIDEO_ID = "id";
    public static final String EXTENSION = "extension";
    public static final String SKIP_FRAMES = "skip_frames";
    public static final String DETECTION_RECTANGLES = "detection_rectangles";

    VideoService videoService;
    @Autowired
    public VideoUtils(VideoService videoService){
        this.videoService = videoService;
    }
    public void analyseVideo(String videoId, String skipFrames, String detectionRectangles) {
        URL url;
        try {
            url = new URL("http://localhost:8081/analysis");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");

            JSONObject body = new JSONObject();
            body.put(VIDEO_ID, videoId);
            body.put(EXTENSION, videoService.getVideo(videoId).getType().split("/")[1]); // hopefully Lob lazily loaded; TODO: check in the future
            body.put(SKIP_FRAMES, skipFrames);
            body.put(DETECTION_RECTANGLES, detectionRectangles);

            System.out.println(body.toString(4));
            byte[] out = body.toString(4).getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage()); // 200 OK
            System.out.println(new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8)); // return value
            connection.disconnect();
        } catch (Exception e) {throw new RuntimeException(e);}
    }
}
