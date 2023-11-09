package app.backend.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    @PositiveOrZero
    private double carFlow;

    @PositiveOrZero
    private int version;

    @NotBlank
    private String timeIntervalId;

    public CarFlow(double carFlow, int version, String timeIntervalId) {
        this.carFlow = carFlow;
        this.version = version;
        this.timeIntervalId = timeIntervalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getCarFlow() {
        return carFlow;
    }

    public void setCarFlow(double carFlowPm) {
        this.carFlow = carFlowPm;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTimeIntervalId() {
        return timeIntervalId;
    }

    public void setTimeIntervalId(String timeIntervalId) {
        this.timeIntervalId = timeIntervalId;
    }
}
