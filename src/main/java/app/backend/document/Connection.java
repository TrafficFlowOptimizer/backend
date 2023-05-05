package app.backend.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "connections")
public class Connection {
    @Id
    private String id;

    private List<String> trafficLightIds;
    private String sourceId;
    private String targetId;
    private List<String> carFlowIds;

    public Connection(List<String> trafficLightIds, String sourceId, String targetId, List<String> carFlowIds) {
        this.trafficLightIds = trafficLightIds;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.carFlowIds = carFlowIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getTrafficLightIds() {
        return trafficLightIds;
    }

    public void setTrafficLightIds(List<String> trafficLightIds) {
        this.trafficLightIds = trafficLightIds;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public List<String> getCarFlowIds() {
        return carFlowIds;
    }

    public void setCarFlowIds(List<String> carFlowIds) {
        this.carFlowIds = carFlowIds;
    }
}
