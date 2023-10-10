package app.backend.request;

import java.util.Vector;

public class DetectionRectangle {
    private String id;
    private Vector<Integer> lowerLeft;
    private Vector<Integer> upperRight;

    public DetectionRectangle(String id, Vector<Integer> lowerLeft, Vector<Integer> upperRight) {
        this.id = id;
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
