package app.backend.controller.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Detection {
    private int id;
    private int detectedCars;
    private int detectedBuses;
    private String connectionId;

    public Detection() {
    }

    public static Detection[] getDetections(String jsonString) throws JsonProcessingException {

        jsonString = jsonString.replaceAll("'", "\"");

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString.substring(1, jsonString.length() - 1), Detection[].class);
    }

    public int getId() {
        return id;
    }

    public int getDetectedCars() {
        return detectedCars;
    }

    public int getDetectedBuses() {
        return detectedBuses;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDetectedCars(int detectedCars) {
        this.detectedCars = detectedCars;
    }

    public void setDetectedBuses(int detectedBuses) {
        this.detectedBuses = detectedBuses;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
