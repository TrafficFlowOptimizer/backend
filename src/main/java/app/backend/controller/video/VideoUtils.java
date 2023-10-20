package app.backend.controller.video;

import app.backend.document.Video;
import app.backend.request.DetectionRectangle;
import app.backend.service.VideoService;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Component
public class VideoUtils {
    public static final String VIDEO_ID = "id";
    public static final String EXTENSION = "extension";
    public static final String SKIP_FRAMES = "skip_frames";
    public static final String DETECTION_RECTANGLES = "detection_rectangles";
    public static final String TEMP_DIRECTORY_PATH = "temp/";

    VideoService videoService;

    @Autowired
    public VideoUtils(VideoService videoService){
        this.videoService = videoService;
    }

    public ResponseEntity<String> analyseVideo(String videoId, int skipFrames, List<DetectionRectangle> detectionRectangles) {
        HttpURLConnection connection;
        try {
            URL url = new URL("http://localhost:8081/analysis"); // TODO: get from variable from environment
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build();
        }
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("Accept", "application/json");

        Video video = videoService.getVideo(videoId);
        if (video == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        JSONObject body = new JSONObject();
        body.put(VIDEO_ID, videoId);
        body.put(EXTENSION, video.getType().split("/")[1]); // hopefully Lob lazily loaded; TODO: check in the future
        body.put(SKIP_FRAMES, skipFrames);
        body.put(DETECTION_RECTANGLES, detectionRectangles);

        System.out.println("INFO:\n" + body.toString(4));

        byte[] out = body.toString(4).getBytes(StandardCharsets.UTF_8);
        OutputStream stream;
        try {
            stream = connection.getOutputStream();
            stream.write(out);

            int responseCode = connection.getResponseCode();
            String responseValue = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            connection.disconnect();

            System.out.println("INFO:\n" + responseCode + " " + responseValue);

            return ResponseEntity
                    .status(responseCode)
                    .body(responseValue);
        } catch (IOException e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    public ResponseEntity<InputStreamResource> getSampleFrame(String videoId) {
        Video video = videoService.getVideo(videoId);

        if (video == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
        String uuid = UUID.randomUUID().toString();
        String videoName = uuid + video.getName();
        String imageName = uuid + "img.jpg";

        if (!createTempVideoFile(video.getData(), videoName)) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build(); // Video data can't be saved to a temp file!
        }

        VideoCapture cap = new VideoCapture();
        cap.open(TEMP_DIRECTORY_PATH + videoName);
        Mat frame = new Mat();

        if (cap.isOpened()) {
            cap.read(frame);
            cap.release();
            Imgcodecs.imwrite(TEMP_DIRECTORY_PATH + imageName, frame);
        } else {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build(); // OpenCV doesn't capture the video
        }

        deleteFiles(TEMP_DIRECTORY_PATH + videoName);

        Path imagePath = Paths.get(TEMP_DIRECTORY_PATH + imageName);
        InputStream in;
        try {
            in = Files.newInputStream(imagePath, StandardOpenOption.DELETE_ON_CLOSE);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build(); // InputStream can't be created
        }
    }

    private boolean createTempVideoFile(byte[] bytes, String name) {
        try (FileOutputStream stream = new FileOutputStream(TEMP_DIRECTORY_PATH + name)) {
            stream.write(bytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void deleteFiles(String... names) {
        for (String name : names) {
            File f = new File(name);

            if (!f.delete()) {
                System.out.println("WARN: Failed to delete temp file " + name);
            }
        }
    }
}
