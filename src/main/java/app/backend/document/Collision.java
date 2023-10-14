package app.backend.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "collisions")
public class Collision {
    @Id
    private String id;

    @PositiveOrZero
    private int index;

    @NotBlank
    private String name;

    @NotBlank
    private String trafficLight1Id;

    @NotBlank
    private String trafficLight2Id;

    private boolean bothCanBeOn;

    public Collision(int index, String name, String trafficLight1Id, String trafficLight2Id, boolean bothCanBeOn) {
        this.index = index;
        this.name = name;
        this.trafficLight1Id = trafficLight1Id;
        this.trafficLight2Id = trafficLight2Id;
        this.bothCanBeOn = bothCanBeOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTrafficLight1Id() {
        return trafficLight1Id;
    }

    public void setTrafficLight1Id(String trafficLight1Id) {
        this.trafficLight1Id = trafficLight1Id;
    }

    public String getTrafficLight2Id() {
        return trafficLight2Id;
    }

    public void setTrafficLight2Id(String trafficLight2Id) {
        this.trafficLight2Id = trafficLight2Id;
    }

    public boolean getBothCanBeOn() {
        return bothCanBeOn;
    }

    public void setBothCanBeOn(boolean bothCanBeOn) {
        this.bothCanBeOn = bothCanBeOn;
    }
}
