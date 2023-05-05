package app.backend.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carflows")
public class CarFlow {
    @Id
    private String id;

    private int carFlowpm;
//    private TimeRange timeRange;
    //?????????????


    public CarFlow(int carFlowpm) {
        this.carFlowpm = carFlowpm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCarFlowpm() {
        return carFlowpm;
    }

    public void setCarFlowpm(int carFlowpm) {
        this.carFlowpm = carFlowpm;
    }
}
