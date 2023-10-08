package app.backend.controller.video;

import app.backend.entity.Video;
import app.backend.service.VideoService;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    public String getSampleFrame(String videoId) {
        try {
            Video vid = videoService.getVideo(videoId);

            String uuid = UUID.randomUUID().toString();
            String videoName = uuid + vid.getName();
            String imageName = uuid + "img.jpg";

            createTempVideoFile(vid.getData(), videoName);

            VideoCapture cap = new VideoCapture();

            String input = "temp/" + videoName;
            String output = "temp/" + imageName;

            cap.open(input);

            Mat frame = new Mat();

            if (cap.isOpened()) {
                cap.read(frame);
            } else {
                throw new Exception("video capture closed");
            }

            deleteTempFiles(videoName);

            return imageName;
        } catch (Exception e) {
            return "";
        }

    }

    private void createTempVideoFile(byte[] bytes, String name) {
        try (FileOutputStream stream = new FileOutputStream("temp/" + name)) {
            stream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTempFiles(String... names) {
        for (String name : names) {
            File f = new File("temp/" + name);

            if (!f.delete()) {
                System.out.println("failed to delete file temp/" + name);
            }
        }
    }
}
