package app.backend.request.crossroad;

import app.backend.document.road.RoadType;

public class RoadRequest {
    private int index;
    private String name;
    private RoadType type;
    private int capacity;

    private Float xCord;
    private Float yCord;

    public RoadRequest(int index, String name, RoadType type, int capacity, Float xCord, Float yCord) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.xCord = xCord;
        this.yCord = yCord;
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

    public Float getxCord() {
        return xCord;
    }

    public void setxCord(Float xCord) {
        this.xCord = xCord;
    }

    public Float getyCord() {
        return yCord;
    }

    public void setyCord(Float yCord) {
        this.yCord = yCord;
    }
}
