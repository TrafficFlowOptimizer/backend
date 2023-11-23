package app.backend.document.light;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trafficlights")
public class TrafficLight {
    @Id
    private String id;

    @PositiveOrZero
    private int index;

    @NotNull
    private TrafficLightDirection direction;

    public TrafficLight(int index, @NotNull TrafficLightDirection direction) {
        this.index = index;
        this.direction = direction;
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

    public @NotNull TrafficLightDirection getDirection() {
        return direction;
    }

    public void setDirection(@NotNull TrafficLightDirection direction) {
        this.direction = direction;
    }
}
