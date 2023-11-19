package app.backend.controller.video;

import app.backend.document.Video;
import app.backend.document.time.Day;
import app.backend.document.time.Hour;
import app.backend.request.DetectionRectangle;
import app.backend.response.VideoInfoResponse;
import app.backend.service.CarFlowService;
import app.backend.service.StartTimeService;
import app.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    private final StartTimeService startTimeService;

    @Autowired
    public VideoController(VideoService videoService, VideoUtils videoUtils, CarFlowService carFlowService, StartTimeService startTimeService) {
        this.videoService = videoService;
        this.videoUtils = videoUtils;
        this.carFlowService = carFlowService;
        this.startTimeService = startTimeService;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile video,
            @RequestParam("crossroadId") String crossroadId,
            @RequestParam("day") Day day,
            @RequestParam("hour") Hour hour,
            @RequestParam("duration") Integer duration
    ) {
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);

        String videoId = videoService.store(video, crossroadId, startTimeId, duration);

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
