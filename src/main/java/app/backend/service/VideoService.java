package app.backend.service;

import app.backend.document.Video;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VideoService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @Autowired
    public VideoService(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public String store(MultipartFile file, String crossroadId, String timeIntervalId) {
        DBObject metadata = new BasicDBObject();
        metadata.put("crossroadId", crossroadId);
        metadata.put("timeIntervalId", timeIntervalId);
        metadata.put("type", file.getContentType());

        ObjectId objectId;
        try {
            objectId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    metadata
            );
        } catch (IOException e) {
            return null;
        }

        return objectId.toString();
    }

    public Video getVideo(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        Video video = null;
        if (file != null && file.getMetadata() != null) {
            try {
                video = new Video(
                        file.getMetadata().get("crossroadId").toString(),
                        file.getFilename(),
                        file.getMetadata().get("type").toString(),
                        file.getMetadata().get("timeIntervalId").toString(),
                        gridFsOperations.getResource(file).getInputStream().readAllBytes()
                );
                video.setId(file.getId().toString());
            } catch (IOException e) {
                return null;
            }
        }

        return video;
    }

    public GridFSFindIterable getAllVideos() {
        return gridFsTemplate.find(new Query());
    }

    public void deleteVideoById(String id) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
    }

    public void deleteVideoByCrossroadId(String crossroadId) {
        gridFsTemplate.delete(new Query(Criteria.where("metadata.crossroadId").is(crossroadId)));
    }
}
