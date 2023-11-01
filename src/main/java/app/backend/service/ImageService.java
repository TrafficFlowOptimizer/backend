package app.backend.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ImageService {
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public String store(String image) {
        DBObject metadata = new BasicDBObject();
        metadata.put("type", "img");

        ObjectId objectId;
        objectId = gridFsTemplate.store(
                new ByteArrayInputStream(image.getBytes(StandardCharsets.UTF_8)),
                UUID.randomUUID().toString(),
                metadata
        );

        return objectId.toString();
    }

    public byte[] getImage(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        byte[] data = null;
        if (file != null) {
            try {
                data = gridFsOperations.getResource(file).getInputStream().readAllBytes();
            } catch (IOException ignored) {
            }
        }

        return data;
    }

    public void deleteImageById(String id) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
    }

    public void deleteAll() {
        gridFsTemplate.delete(new Query(Criteria.where("metadata.type").is("img")));
    }
}
