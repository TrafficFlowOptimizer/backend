package app.backend.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    private int carFlowPm;

    private LocalTime startTime;

    private LocalTime endTime;

    public CarFlow(int carFlowPm, LocalTime startTime, LocalTime endTime) {
        this.carFlowPm = carFlowPm;
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
        return carFlowPm;
    }

    public void setCarFlowPm(int carFlowPm) {
        this.carFlowPm = carFlowPm;
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
