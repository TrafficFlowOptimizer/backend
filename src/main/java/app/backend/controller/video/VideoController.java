package app.backend.controller.video;

import app.backend.document.Video;
import app.backend.request.DetectionRectangle;
import app.backend.response.VideoResponseFile;
import app.backend.response.VideoResponseMessage;
import app.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    public ResponseEntity<VideoResponseMessage> upload(
            @RequestParam("file") MultipartFile video,
            @RequestParam("crossroadId") String crossroadId,
            @RequestParam("timeIntervalId") String timeIntervalId
    ) {
        String videoId = videoService.store(video, crossroadId, timeIntervalId);

        if (videoId != null) {
            String message = "Video: " + video.getOriginalFilename() + " uploaded successfully with id: " + videoId;
            return ResponseEntity
                    .ok()
                    .body(new VideoResponseMessage(message));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new VideoResponseMessage("Video upload failed!"));
        }
    }

    @GetMapping(value="/{id}/sample")
    public ResponseEntity<InputStreamResource> sample(@PathVariable String id) {
        return videoUtils.getSampleFrame(id);
    }

    @GetMapping(value="/{id}/analysis")
    public ResponseEntity<String> analyse(
            @PathVariable String id,
            @RequestParam int skipFrames,
            @RequestParam List<DetectionRectangle> detectionRectangles
    ) {
        return videoUtils.analyseVideo(id, skipFrames, detectionRectangles);
    }

    @GetMapping
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
                        video.getData().length
                );
            }).collect(Collectors.toList());

            return ResponseEntity
                    .ok()
                    .body(videos);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        Video video = videoService.getVideo(id);

        if (video != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + video.getName() + "\"")
                    .body(video.getData());
        } else {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        videoService.deleteVideoById(id);

        return ResponseEntity
                .ok()
                .body("Video id: " + id + "no longer stored");
    }
}
