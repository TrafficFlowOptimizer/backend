package app.backend.request.crossroad;

import app.backend.document.collision.CollisionType;

public class CollisionRequest {
    private int index;
    private String name;
    private int trafficLight1Id;
    private int trafficLight2Id;
    private CollisionType type;

    public CollisionRequest(int index, String name, int trafficLight1Id, int trafficLight2Id, CollisionType type) {
        this.index = index;
        this.name = name;
        this.trafficLight1Id = trafficLight1Id;
        this.trafficLight2Id = trafficLight2Id;
        this.type = type;
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

    public int getTrafficLight1Id() {
        return trafficLight1Id;
    }

    public void setTrafficLight1Id(int trafficLight1Id) {
        this.trafficLight1Id = trafficLight1Id;
    }

    public int getTrafficLight2Id() {
        return trafficLight2Id;
    }

    public void setTrafficLight2Id(int trafficLight2Id) {
        this.trafficLight2Id = trafficLight2Id;
    }

    public CollisionType getType() {
        return type;
    }

    public void setType(CollisionType type) {
        this.type = type;
    }
}
