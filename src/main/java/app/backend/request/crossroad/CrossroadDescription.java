package app.backend.request.crossroad;

import java.util.List;

public class CrossroadDescription {
    private CrossroadRequest crossroad;
    private List<RoadRequest> roads;
    private List<CollisionRequest> collisions;
    private List<ConnectionRequest> connections;
    private List<TrafficLightRequest> trafficLights;

    public CrossroadDescription(
            CrossroadRequest crossroad,
            List<RoadRequest> roads,
            List<CollisionRequest> collisions,
            List<ConnectionRequest> connections,
            List<TrafficLightRequest> trafficLights
    ) {
        this.crossroad = crossroad;
        this.roads = roads;
        this.collisions = collisions;
        this.connections = connections;
        this.trafficLights = trafficLights;
    }

    public CrossroadRequest getCrossroad() {
        return crossroad;
    }

    public void setCrossroad(CrossroadRequest crossroad) {
        this.crossroad = crossroad;
    }

    public List<RoadRequest> getRoads() {
        return roads;
    }

    public void setRoads(List<RoadRequest> roads) {
        this.roads = roads;
    }

    public List<CollisionRequest> getCollisions() {
        return collisions;
    }

    public void setCollisions(List<CollisionRequest> collisions) {
        this.collisions = collisions;
    }

    public List<ConnectionRequest> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionRequest> connections) {
        this.connections = connections;
    }

    public List<TrafficLightRequest> getTrafficLights() {
        return trafficLights;
    }

    public void setTrafficLights(List<TrafficLightRequest> trafficLights) {
        this.trafficLights = trafficLights;
    }
}
