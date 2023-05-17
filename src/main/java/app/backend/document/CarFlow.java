package app.backend.document;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import app.backend.validation.StartBeforeEndTime;

import java.time.LocalTime;

@StartBeforeEndTime
@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    @PositiveOrZero
    private int carFlow;

    // TODO: is LocalTime the right type??
    private LocalTime startTime;

    private LocalTime endTime;

    public CarFlow(int carFlow, LocalTime startTime, LocalTime endTime) {
        this.carFlow = carFlow;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCarFlowPm() {
        return carFlow;
    }

    public void setCarFlowPm(int carFlowPm) {
        this.carFlow = carFlowPm;
    }

    public void setCarStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
