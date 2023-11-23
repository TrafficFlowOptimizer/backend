package app.backend.request.crossroad;

import app.backend.document.light.TrafficLightDirection;

public class TrafficLightRequest {
    private int index;
    private TrafficLightDirection direction;

    public TrafficLightRequest(int index, TrafficLightDirection direction) {
        this.index = index;
        this.direction = direction;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TrafficLightDirection getDirection() {
        return direction;
    }

    public void setDirection(TrafficLightDirection direction) {
        this.direction = direction;
    }
}
