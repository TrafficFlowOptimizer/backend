package app.backend.request.optimization;

import java.util.List;

public class OptimizationRequest {
    private int time;
    private int roadsCount;
    private List<Integer> lightCollisions;
    private int lightCollisionsCount;
    private List<Integer> heavyCollisions;
    private int heavyCollisionsCount;
    private List<Integer> roadsConnections;
    private int connectionsCount;
    private int carFlowPerMinute;
    private int lightsCount;
    private int timeUnitsInMinute;
    private int numberOfTimeUnits;
    private int lightsType;
    private List<Integer> lights;

    public OptimizationRequest(
            int time,
            int roadsCount,
            List<Integer> lightCollisions,
            int lightCollisionsCount,
            List<Integer> heavyCollisions,
            int heavyCollisionsCount,
            List<Integer> roadsConnections,
            int connectionsCount,
            int carFlowPerMinute,
            int lightsCount,
            int timeUnitsInMinute,
            int numberOfTimeUnits,
            int lightsType,
            List<Integer> lights
    ) {
        this.time = time;
        this.roadsCount = roadsCount;
        this.lightCollisions = lightCollisions;
        this.lightCollisionsCount = lightCollisionsCount;
        this.heavyCollisions = heavyCollisions;
        this.heavyCollisionsCount = heavyCollisionsCount;
        this.roadsConnections = roadsConnections;
        this.connectionsCount = connectionsCount;
        this.carFlowPerMinute = carFlowPerMinute;
        this.lightsCount = lightsCount;
        this.timeUnitsInMinute = timeUnitsInMinute;
        this.numberOfTimeUnits = numberOfTimeUnits;
        this.lightsType = lightsType;
        this.lights = lights;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getRoadsCount() {
        return roadsCount;
    }

    public void setRoadsCount(int roadsCount) {
        this.roadsCount = roadsCount;
    }

    public List<Integer> getLightCollisions() {
        return lightCollisions;
    }

    public void setLightCollisions(List<Integer> lightCollisions) {
        this.lightCollisions = lightCollisions;
    }

    public int getLightCollisionsCount() {
        return lightCollisionsCount;
    }

    public void setLightCollisionsCount(int lightCollisionsCount) {
        this.lightCollisionsCount = lightCollisionsCount;
    }

    public List<Integer> getHeavyCollisions() {
        return heavyCollisions;
    }

    public void setHeavyCollisions(List<Integer> heavyCollisions) {
        this.heavyCollisions = heavyCollisions;
    }

    public int getHeavyCollisionsCount() {
        return heavyCollisionsCount;
    }

    public void setHeavyCollisionsCount(int heavyCollisionsCount) {
        this.heavyCollisionsCount = heavyCollisionsCount;
    }

    public List<Integer> getRoadsConnections() {
        return roadsConnections;
    }

    public void setRoadsConnections(List<Integer> roadsConnections) {
        this.roadsConnections = roadsConnections;
    }

    public int getConnectionsCount() {
        return connectionsCount;
    }

    public void setConnectionsCount(int connectionsCount) {
        this.connectionsCount = connectionsCount;
    }

    public int getCarFlowPerMinute() {
        return carFlowPerMinute;
    }

    public void setCarFlowPerMinute(int carFlowPerMinute) {
        this.carFlowPerMinute = carFlowPerMinute;
    }

    public int getLightsCount() {
        return lightsCount;
    }

    public void setLightsCount(int lightsCount) {
        this.lightsCount = lightsCount;
    }

    public int getTimeUnitsInMinute() {
        return timeUnitsInMinute;
    }

    public void setTimeUnitsInMinute(int timeUnitsInMinute) {
        this.timeUnitsInMinute = timeUnitsInMinute;
    }

    public int getNumberOfTimeUnits() {
        return numberOfTimeUnits;
    }

    public void setNumberOfTimeUnits(int numberOfTimeUnits) {
        this.numberOfTimeUnits = numberOfTimeUnits;
    }

    public int getLightsType() {
        return lightsType;
    }

    public void setLightsType(int lightsType) {
        this.lightsType = lightsType;
    }

    public List<Integer> getLights() {
        return lights;
    }

    public void setLights(List<Integer> lights) {
        this.lights = lights;
    }
}
