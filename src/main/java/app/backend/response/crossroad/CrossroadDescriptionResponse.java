package app.backend.response.crossroad;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.road.Road;

import java.util.List;

public class CrossroadDescriptionResponse {
    private Crossroad crossroad;
    private List<Road> roads;
    private List<Collision> collisions;
    private List<Connection> connections;
    private List<TrafficLight> trafficLights;
    private String image;

    public CrossroadDescriptionResponse(
            Crossroad crossroad,
            List<Road> roads,
            List<Collision> collisions,
            List<Connection> connections,
            List<TrafficLight> trafficLights,
            String image
    ) {
        this.crossroad = crossroad;
        this.roads = roads;
        this.collisions = collisions;
        this.connections = connections;
        this.trafficLights = trafficLights;
        this.image = image;
    }

    public Crossroad getCrossroad() {
        return crossroad;
    }

    public void setCrossroad(Crossroad crossroad) {
        this.crossroad = crossroad;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public void setRoads(List<Road> roads) {
        this.roads = roads;
    }

    public List<Collision> getCollisions() {
        return collisions;
    }

    public void setCollisions(List<Collision> collisions) {
        this.collisions = collisions;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }

    public void setTrafficLights(List<TrafficLight> trafficLights) {
        this.trafficLights = trafficLights;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
