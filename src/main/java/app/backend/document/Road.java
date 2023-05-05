package app.backend.document;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "roads")
public class Road {
    @Id
    private String id;

    private String name;
    private RoadType type;
    private int capacity;

    public Road(String name, RoadType type) {
        this.name = name;
        this.type = type;
    }

    public Road(String name, RoadType type, int capacity) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
