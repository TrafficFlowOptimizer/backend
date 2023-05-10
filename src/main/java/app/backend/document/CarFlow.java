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
    private int carFlowPm;

    // TODO: czy local time czy coś innego uwzględniającego dnie
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
