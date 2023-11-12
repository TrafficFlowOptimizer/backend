package app.backend.controller.video;

import app.backend.document.Video;
import app.backend.request.DetectionRectangle;
import app.backend.response.VideoInfoResponse;
import app.backend.service.CarFlowService;
import app.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@CrossOrigin("*")
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;
    private final VideoUtils videoUtils;
    private final CarFlowService carFlowService;

    @Autowired
    public VideoController(VideoService videoService, VideoUtils videoUtils, CarFlowService carFlowService) {
        this.videoService = videoService;
        this.videoUtils = videoUtils;
        this.carFlowService = carFlowService;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile video,
            @RequestParam("crossroadId") String crossroadId,
            @RequestParam("timeIntervalId") String timeIntervalId,
            @RequestParam("duration") Integer duration
    ) {
        String videoId = videoService.store(video, crossroadId, timeIntervalId, duration);

        if (videoId != null) {
            return ResponseEntity
                    .ok()
                    .body("Video: " + video.getOriginalFilename() + " uploaded successfully with id: " + videoId);
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body("Video upload failed!");
        }
    }

    @GetMapping(value = "/{id}/sample")
    public ResponseEntity<InputStreamResource> sample(@PathVariable String id) {
        return videoUtils.getSampleFrame(id);
    }

    @PostMapping(value = "/{id}/analysis")
    public ResponseEntity<Detection[]> analyse(
            @PathVariable String id,
            @RequestParam int skipFrames,
            @RequestBody List<DetectionRectangle> detectionRectangles
    ) {
        Detection[] detections = videoUtils.analyseVideo(id, skipFrames, detectionRectangles);
        return ResponseEntity
                .ok()
                .body(detections);
    }

    @GetMapping
    public ResponseEntity<List<VideoInfoResponse>> list() {
        List<VideoInfoResponse> videos = Streamable.of(videoService.getAllVideos()
                        .map(video -> {
                            String fileDownloadUri = ServletUriComponentsBuilder
                                    .fromCurrentContextPath()
                                    .path("/videos/")
                                    .path(video.getId().asObjectId().getValue().toString())
                                    .toUriString();

                            if (video.getMetadata() != null) {
                                return new VideoInfoResponse(
                                        video.getFilename(),
                                        fileDownloadUri,
                                        video.getMetadata().get("type").toString(),
                                        video.getLength()
                                );
                            } else {
                                return null;
                            }
                        }))
                .toList();

        return ResponseEntity
                .ok()
                .body(videos);
    }

    @GetMapping(value = "/{id}")
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
