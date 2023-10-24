package app.backend.service;

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
public class ImageService {
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public String store(MultipartFile file) {
        ObjectId objectId;
        try {
            objectId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    null
            );
        } catch (IOException e) {
            return null;
        }

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
}
