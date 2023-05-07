package app.backend.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trafficlights")
public class TrafficLight {
    @Id
    private String id;

    public TrafficLight() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
