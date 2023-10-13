package app.backend.request.crossroad;

import java.util.List;

public class ConnectionRequest {
    private int index;
    private String name;
    private List<Integer> trafficLightIds;
    private int sourceId;
    private int targetId;

    public ConnectionRequest(int index, String name, List<Integer> trafficLightIds, int sourceId, int targetId) {
        this.index = index;
        this.name = name;
        this.trafficLightIds = trafficLightIds;
        this.sourceId = sourceId;
        this.targetId = targetId;
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

    public List<Integer> getTrafficLightIds() {
        return trafficLightIds;
    }

    public void setTrafficLightIds(List<Integer> trafficLightIds) {
        this.trafficLightIds = trafficLightIds;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
}
