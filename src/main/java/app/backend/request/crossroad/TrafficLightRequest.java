package app.backend.request.crossroad;

import app.backend.document.light.TrafficLightType;

public class TrafficLightRequest {
    private int index;
    private String name;
    private TrafficLightType type;

    public TrafficLightRequest(int index, String name, TrafficLightType type) {
        this.index = index;
        this.name = name;
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

    public TrafficLightType getType() {
        return type;
    }

    public void setType(TrafficLightType type) {
        this.type = type;
    }
}
