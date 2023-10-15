package app.backend.request.crossroad;

import app.backend.document.crossroad.CrossroadType;

import java.util.List;

public class CrossroadRequest {
    private String name;
    private String location;
    private CrossroadType type;
    private List<Integer> roadIds;
    private List<Integer> collisionIds;
    private List<Integer> connectionIds;
    private List<Integer> trafficLightsIds;

    public CrossroadRequest(
            String name,
            String location,
            CrossroadType type,
            List<Integer> roadIds,
            List<Integer> collisionIds,
            List<Integer> connectionIds,
            List<Integer> trafficLightsIds
    ) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.roadIds = roadIds;
        this.collisionIds = collisionIds;
        this.connectionIds = connectionIds;
        this.trafficLightsIds = trafficLightsIds;
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

    public CrossroadType getType() {
        return type;
    }

    public void setType(CrossroadType type) {
        this.type = type;
    }

    public List<Integer> getRoadIds() {
        return roadIds;
    }

    public void setRoadIds(List<Integer> roadIds) {
        this.roadIds = roadIds;
    }

    public List<Integer> getCollisionIds() {
        return collisionIds;
    }

    public void setCollisionIds(List<Integer> collisionIds) {
        this.collisionIds = collisionIds;
    }

    public List<Integer> getConnectionIds() {
        return connectionIds;
    }

    public void setConnectionIds(List<Integer> connectionIds) {
        this.connectionIds = connectionIds;
    }

    public List<Integer> getTrafficLightsIds() {
        return trafficLightsIds;
    }

    public void setTrafficLightsIds(List<Integer> trafficLightsIds) {
        this.trafficLightsIds = trafficLightsIds;
    }
}
