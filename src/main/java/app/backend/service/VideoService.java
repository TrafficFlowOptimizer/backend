package app.backend.service;

import app.backend.entity.Video;
import app.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video store(MultipartFile file, String crossroadId, String timeIntervalId) {
        String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            return videoRepository.save(
                    new Video(
                            crossroadId,
                            name,
                            file.getContentType(),
                            timeIntervalId,
                            file.getBytes()
                    )
            );
        } catch (IOException e) {
            return null;
        }
    }

    public Video getVideo(String id) {
        return videoRepository
                .findById(id)
                .orElse(null);
    }

    public Stream<Video> getAllVideos() {
        return videoRepository
                .findAll()
                .stream();
    }

    public void deleteVideoById(String id){
        videoRepository.deleteById(id);
    }
}
