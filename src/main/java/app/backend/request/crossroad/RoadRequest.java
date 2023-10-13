package app.backend.request.crossroad;

import app.backend.document.road.RoadType;

public class RoadRequest {
    private int index;
    private String name;
    private RoadType type;
    private int capacity;

    public RoadRequest(int index, String name, RoadType type, int capacity) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
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

    public RoadType getType() {
        return type;
    }

    public void setType(RoadType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
