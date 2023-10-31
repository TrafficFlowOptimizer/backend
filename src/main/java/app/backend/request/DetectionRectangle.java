package app.backend.request;

import java.util.Vector;

public class DetectionRectangle {
    private String id;
    private String connectionId;
    private Vector<Integer> lowerLeft;
    private Vector<Integer> upperRight;

    public DetectionRectangle(String id, String connectionId, Vector<Integer> lowerLeft, Vector<Integer> upperRight) {
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

    public Vector<Integer> getLowerLeft() {
        return lowerLeft;
    }

    public void setLowerLeft(Vector<Integer> lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public Vector<Integer> getUpperRight() {
        return upperRight;
    }

    public void setUpperRight(Vector<Integer> upperRight) {
        this.upperRight = upperRight;
    }
}
