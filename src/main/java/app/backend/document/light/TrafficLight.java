package app.backend.document.light;

import jakarta.validation.constraints.NotBlank;
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
    private TrafficLightType type;

    public TrafficLight(int index, @NotNull TrafficLightType type) {
        this.index = index;
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

    public @NotNull TrafficLightType getType() {
        return type;
    }

    public void setType(@NotNull TrafficLightType type) {
        this.type = type;
    }
}
