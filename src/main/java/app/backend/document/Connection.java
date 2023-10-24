package app.backend.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "connections")
public class Connection {
    @Id
    private String id;

    @PositiveOrZero
    private int index;

    @NotBlank
    private String name;

    private List<String> trafficLightIds;

    @NotBlank
    private String sourceId;

    @NotBlank
    private String targetId;

    //@NotEmpty?
    private List<String> carFlowIds;

    public Connection(int index, String name, List<String> trafficLightIds, String sourceId, String targetId, List<String> carFlowIds) {
        this.index = index;
        this.name = name;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
