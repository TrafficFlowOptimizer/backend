package app.backend.service;

import app.backend.entity.Video;
import app.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public void store(MultipartFile file, String crossroadId, String timeIntervalId) throws IOException {
        String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        videoRepository.save( new Video(crossroadId, name, file.getContentType(), timeIntervalId, file.getBytes()) );
    }

    public Video getVideo(String id) {
        return videoRepository.findById(id).get();
    }

    public Stream<Video> getAllVideos() {
        return videoRepository.findAll().stream();
    }

}
