package app.backend.request;

import org.springframework.data.util.Pair;

public class DetectionRectangle {
    private String id;
    private String connectionId;
    private Pair<Integer, Integer> lowerLeft;
    private Pair<Integer, Integer> upperRight;

    public DetectionRectangle(String id, String connectionId, Pair<Integer,
            Integer> lowerLeft, Pair<Integer, Integer> upperRight) {
        this.id = id;
        this.connectionId = connectionId;
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public Pair<Integer, Integer> getLowerLeft() {
        return lowerLeft;
    }

    public void setLowerLeft(Pair<Integer, Integer> lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public Pair<Integer, Integer> getUpperRight() {
        return upperRight;
    }

    public void setUpperRight(Pair<Integer, Integer> upperRight) {
        this.upperRight = upperRight;
    }
}
