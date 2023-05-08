package app.backend.document.crossroad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "crossroads")
public class Crossroad {
    @Id
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String location;

    @NotBlank
    private String creator;

    @NotNull
    private CrossroadType type;

    //@NotEmpty
    private List<String> roadIds;
    //@NotEmpty
    private List<String> collisionIds;

    public Crossroad(String name, String location, String creator, CrossroadType type, List<String> roadIds, List<String> collisionIds) {
        this.name = name;
        this.location = location;
        this.creator = creator;
        this.type = type;
        this.roadIds = roadIds;
        this.collisionIds = collisionIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public CrossroadType getType() {
        return type;
    }

    public void setType(CrossroadType type) {
        this.type = type;
    }

    public List<String> getRoadIds() {
        return roadIds;
    }

    public void setRoadIds(List<String> roadIds) {
        this.roadIds = roadIds;
    }

    public List<String> getCollisionIds() {
        return collisionIds;
    }

    public void setCollisionIds(List<String> collisionIds) {
        this.collisionIds = collisionIds;
    }
}
