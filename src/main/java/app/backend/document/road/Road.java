package app.backend.document.road;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roads")
public class Road {
    @Id
    private String id;

    @PositiveOrZero
    private int index;

    @NotBlank
    private String name;

    @NotNull
    private RoadType type;

    @PositiveOrZero
    private Float xCord;

    @PositiveOrZero
    private Float yCord;

    @PositiveOrZero
    private int capacity;

    public Road(int index, String name, @NotNull RoadType type, int capacity, Float xCord, Float yCord) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.yCord = yCord;
        this.xCord = xCord;
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

    public @NotNull RoadType getType() {
        return type;
    }

    public void setType(@NotNull RoadType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Float getxCord() {
        return xCord;
    }

    public void setxCord(Float xCord) {
        this.xCord = xCord;
    }

    public Float getyCord() {
        return yCord;
    }

    public void setyCord(Float yCord) {
        this.yCord = yCord;
    }
}
