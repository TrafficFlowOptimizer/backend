package app.backend.request.crossroad;

public class CollisionRequest {
    private int index;
    private String name;
    private int connection1Id;
    private int connection2Id;
    private boolean bothCanBeOn;

    public CollisionRequest(int index, String name, int connection1Id, int connection2Id, boolean bothCanBeOn) {
        this.index = index;
        this.name = name;
        this.connection1Id = connection1Id;
        this.connection2Id = connection2Id;
        this.bothCanBeOn = bothCanBeOn;
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

    public int getConnection1Id() {
        return connection1Id;
    }

    public void setConnection1Id(int connection1Id) {
        this.connection1Id = connection1Id;
    }

    public int getConnection2Id() {
        return connection2Id;
    }

    public void setConnection2Id(int connection2Id) {
        this.connection2Id = connection2Id;
    }

    public boolean getBothCanBeOn() {
        return bothCanBeOn;
    }

    public void setBothCanBeOn(boolean bothCanBeOn) {
        this.bothCanBeOn = bothCanBeOn;
    }
}
