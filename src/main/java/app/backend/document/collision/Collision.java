package app.backend.document.collision;

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

    @NotNull
    private CollisionType type;

    public Collision(int index, String trafficLight1Id, String trafficLight2Id, CollisionType type) {
        this.index = index;
        this.trafficLight1Id = trafficLight1Id;
        this.trafficLight2Id = trafficLight2Id;
        this.type = type;
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

    public CollisionType getType() {
        return type;
    }

    public void setType(CollisionType type) {
        this.type = type;
    }
}
