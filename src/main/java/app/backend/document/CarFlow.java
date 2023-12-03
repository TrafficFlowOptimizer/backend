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
    private int carFlow;

    @PositiveOrZero
    private int version;

    @NotBlank
    private String startTimeId;

    public CarFlow(int carFlow, String startTimeId, int version) {
        this.carFlow = carFlow;
        this.version = version;
        this.startTimeId = startTimeId;
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

    public void setCarFlow(int carFlowPm) {
        this.carFlow = carFlowPm;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getStartTimeId() {
        return startTimeId;
    }

    public void setStartTimeId(String startTimeId) {
        this.startTimeId = startTimeId;
    }
}
