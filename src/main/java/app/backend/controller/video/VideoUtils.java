package app.backend.controller.video;

import app.backend.service.VideoService;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class VideoUtils {

    @Autowired
    VideoService videoService;

    public void analyseVideo(String videoId) {
        URL url;
        try {
            url = new URL("http://localhost:8081/analysis");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");

            JSONObject body = new JSONObject();
            body.put("id", videoId);
            body.put("extension", videoService.getVideo(videoId).getType().split("/")[1]); // hopefully Lob lazily loaded; TODO: check in the future

            System.out.println(body.toString(4));
            byte[] out = body.toString(4).getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage()); // 200 OK
            System.out.println(new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8)); // return value
            connection.disconnect();
        } catch (Exception e) {throw new RuntimeException(e);}
    }

    public void getSampleFrame(String videoId) {
        VideoCapture cap = new VideoCapture();

        String input = "temp/sample1.mp4";
        String output = "temp/resimg.jpg";

        cap.open(input);

        Mat frame = new Mat();

        if (cap.isOpened()) {
            cap.read(frame);

            Imgcodecs.imwrite(output, frame);
        } else {
            System.out.println("Fail");
        }
    }
}
