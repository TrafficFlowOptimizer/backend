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

    // TODO: Location type might be better
    @NotBlank
    private String location;

    @NotBlank
    private String creatorId;

    @NotNull
    private CrossroadType type;

    //@NotEmpty
    private List<String> roadIds;
    //@NotEmpty
    private List<String> collisionIds;
    //@NotEmpty
    private List<String> connectionIds;
    //@NotEmpty
    private List<String> trafficLightIds;

    public Crossroad(String name, String location, String creatorId, CrossroadType type, List<String> roadIds, List<String> collisionIds, List<String> connectionIds, List<String> trafficLightIds) {
        this.name = name;
        this.location = location;
        this.creatorId = creatorId;
        this.type = type;
        this.roadIds = roadIds;
        this.collisionIds = collisionIds;
        this.connectionIds = connectionIds;
        this.trafficLightIds = trafficLightIds;
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
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

    public List<String> getConnectionIds() {
        return connectionIds;
    }

    public void setConnectionIds(List<String> connectionIds) {
        this.connectionIds = connectionIds;
    }

    public List<String> getTrafficLightIds() {
        return trafficLightIds;
    }

    public void setTrafficLightIds(List<String> trafficLightIds) {
        this.trafficLightIds = trafficLightIds;
    }
}
