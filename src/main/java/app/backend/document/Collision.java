package app.backend.document;

import jakarta.validation.constraints.NotBlank;
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
    private String connection1Id;

    @NotBlank
    private String connection2Id;

    private boolean bothCanBeOn;

    public Collision(int index, String name, String connection1Id, String connection2Id, boolean bothCanBeOn) {
        this.index = index;
        this.name = name;
        this.connection1Id = connection1Id;
        this.connection2Id = connection2Id;
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

    public String getConnection1Id() {
        return connection1Id;
    }

    public void setConnection1Id(String connection1Id) {
        this.connection1Id = connection1Id;
    }

    public String getConnection2Id() {
        return connection2Id;
    }

    public void setConnection2Id(String connection2Id) {
        this.connection2Id = connection2Id;
    }

    public boolean getBothCanBeOn() {
        return bothCanBeOn;
    }

    public void setBothCanBeOn(boolean bothCanBeOn) {
        this.bothCanBeOn = bothCanBeOn;
    }
}
