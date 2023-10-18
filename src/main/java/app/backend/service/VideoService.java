package app.backend.service;

import app.backend.document.Video;
import app.backend.repository.VideoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @Autowired
    public VideoService(VideoRepository videoRepository, GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.videoRepository = videoRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public Video store(MultipartFile file, String crossroadId, String timeIntervalId) {
        DBObject metadata = new BasicDBObject();

        Object fileID = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );

        return fileID.toString();

//        try {
//            return videoRepository.save(
//                    new Video(
//                            crossroadId,
//                            name,
//                            file.getContentType(),
//                            timeIntervalId,
//                            file.getBytes()
//                    )
//            );
//        } catch (IOException e) {
//            return null;
//        }
    }

    public Video getVideo(String id) {
//        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

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
