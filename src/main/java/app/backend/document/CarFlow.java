package app.backend.document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import app.backend.validation.StartBeforeEndTime;

import java.time.LocalTime;

@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    @PositiveOrZero
    private double carFlow;

    @NotNull
    private TimeInterval timeIntervalId;

    public CarFlow(int carFlow, TimeInterval timeIntervalId) {
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

    public TimeInterval getTimeIntervalId() {
        return timeIntervalId;
    }

    public void setTimeIntervalId(TimeInterval timeIntervalId) {
        this.timeIntervalId = timeIntervalId;
    }
}
