package app.backend.service;

import app.backend.entity.Video;
import app.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    private final static String videosPath = "";

    @Value("${video.path}")
    private String videoPath;

    @Autowired
    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }
    public Video store(MultipartFile file, String crossroadId, String timeIntervalId) throws IOException {
        String name = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        String extension = file.getOriginalFilename().split("\\.")[1];
        Video video = videoRepository.save(new Video(crossroadId, name, file.getContentType(), timeIntervalId, file.getBytes()));
        saveVideoInDirectory(file, video.getId(), extension);
        return video;
    }

    public Video getVideo(String id) throws Exception {
        Optional<Video> video = videoRepository.findById(id);

        if (video.isPresent()) {
            return video.get();
        }
        throw new Exception("No video for id " + id + " found in database");
    }

    public Stream<Video> getAllVideos() {
        return videoRepository.findAll().stream();
    }

    public void deleteVideoById(String id){
        videoRepository.deleteById(id);
    }

    private void saveVideoInDirectory(MultipartFile video, String videoId, String videoType) {
        try {
            OutputStream out = new FileOutputStream(videoPath + "\\" + videoId + "." + videoType);
            out.write(video.getBytes());
            out.close();
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }

}
