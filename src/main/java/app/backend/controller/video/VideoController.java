package app.backend.controller.video;

import app.backend.entity.Video;
import app.backend.response.VideoResponseFile;
import app.backend.response.VideoResponseMessage;
import app.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

// https://www.bezkoder.com/spring-boot-upload-file-database/

@RestController
@CrossOrigin("http://localhost:8081")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping(value="/videos/upload")
    public ResponseEntity<VideoResponseMessage> upload(@RequestParam("file") MultipartFile video) {
        String message;
        try {
            videoService.store(video);

            message = "Uploaded the video successfully: " + video.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new VideoResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the video: " + video.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new VideoResponseMessage(message));
        }
    }

    @GetMapping(value="/videos")
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

    @GetMapping(value="/videos/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        Video video = videoService.getVideo(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + video.getName() + "\"")
                .body(video.getData());
    }

}
