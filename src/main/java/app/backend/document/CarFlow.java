package app.backend.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    @PositiveOrZero
    private double carFlow;

    @NotBlank
    private String timeIntervalId;

    public CarFlow(int carFlow, String timeIntervalId) {
        this.carFlow = carFlow;
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

    public String getTimeIntervalId() {
        return timeIntervalId;
    }

    public void setTimeIntervalId(String timeIntervalId) {
        this.timeIntervalId = timeIntervalId;
    }
}
