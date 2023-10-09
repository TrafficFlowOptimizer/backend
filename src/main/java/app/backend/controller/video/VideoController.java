package app.backend.controller.video;

import app.backend.entity.Video;
import app.backend.response.VideoResponseFile;
import app.backend.response.VideoResponseMessage;
import app.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import static app.backend.controller.video.VideoUtils.DETECTION_RECTANGLES;
import static app.backend.controller.video.VideoUtils.SKIP_FRAMES;

// https://www.bezkoder.com/spring-boot-upload-file-database/

@RestController
@CrossOrigin("http://localhost:8081")
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;
    private final VideoUtils videoUtils;

    @Autowired
    public VideoController(VideoService videoService, VideoUtils videoUtils) {
        this.videoService = videoService;
        this.videoUtils = videoUtils;
    }

    @PostMapping(value="/upload")
    public ResponseEntity<VideoResponseMessage> upload(@RequestParam("file") MultipartFile video,
                                                       @RequestParam("crossroadId") String crossroadId,
                                                       @RequestParam("timeIntervalId") String timeIntervalId) {
        String message;
        Video storedVideo = null;
        try {
            storedVideo = videoService.store(video, crossroadId, timeIntervalId);
            message = "Uploaded the video successfully: " + video.getOriginalFilename();

            return ResponseEntity.status(HttpStatus.OK).body(new VideoResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the video: " + video.getOriginalFilename() + "!";
            if (storedVideo != null) {
                videoService.deleteVideoById(storedVideo.getId());
            } // czy potrzebne teraz?
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new VideoResponseMessage(message));
        }
    }

    @GetMapping(value="/{id}/sample")
    public ResponseEntity<InputStreamResource> sample(@PathVariable String id) {
        ResponseEntity<InputStreamResource> response;

        String name = videoUtils.getSampleFrame(id);

        if (name.length() == 0) {
            response = ResponseEntity.unprocessableEntity().build();
        } else {
            InputStream in;
            try {
                Path img = Paths.get(name);
                in = Files.newInputStream(img, StandardOpenOption.DELETE_ON_CLOSE);
                response = ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new InputStreamResource(in));
            } catch (IOException e) {
                response = ResponseEntity.unprocessableEntity().build();
            }
        }

        VideoUtils.deleteFiles(name);
        return response;
    }

    @GetMapping(value="/{id}/analysis")
    public String analyse(@PathVariable String id,
                          @RequestParam String skipFrames,
                          @RequestParam String detectionRectangles) {
        videoUtils.analyseVideo(id, skipFrames, detectionRectangles);

        return "true";
    }

    @GetMapping(value="")
    public ResponseEntity<List<VideoResponseFile>> list() {
            List<VideoResponseFile> videos = videoService.getAllVideos().map(video -> {
                String fileDownloadUri = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/videos/")
                        .path(video.getId())
                        .toUriString();

                return new VideoResponseFile(
                        video.getName(),
                        fileDownloadUri,
                        video.getType(),
                        video.getData().length);
            }).collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) throws Exception {
        Video video = videoService.getVideo(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + video.getName() + "\"")
                .body(video.getData());
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable String id){
        videoService.deleteVideoById(id);
        return ResponseEntity.ok().body("Video id: " + id + "successfully deleted or was not even there");
    }
}
