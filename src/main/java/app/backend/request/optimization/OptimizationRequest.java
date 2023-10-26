package app.backend.request.optimization;

import app.backend.document.light.TrafficLightType;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class OptimizationRequest {
    private int optimizationTime;
    private int roadsCount;
    private List<Pair<Integer, Integer>> lightCollisions;
    private int lightCollisionsCount;
    private List<Pair<Integer, Integer>> heavyCollisions;
    private int heavyCollisionsCount;
    private List<List<Integer>> roadsConnections;
    private int connectionsCount;
    private List<Double> carFlowPerMinute;
    private int lightsCount;
    private int timeUnitsInMinute;
    private int numberOfTimeUnits;
    private List<TrafficLightType> lightsType;


    public OptimizationRequest() {
        this(0, 0, new ArrayList<>(), 0, new ArrayList<>(), 0, new ArrayList<>(), 0, new ArrayList<>(), 0, 0, 0, new ArrayList<>());
    }

    public OptimizationRequest(
            int optimizationTime,
            int roadsCount,
            List<Pair<Integer, Integer>> lightCollisions,
            int lightCollisionsCount,
            List<Pair<Integer, Integer>> heavyCollisions,
            int heavyCollisionsCount,
            List<List<Integer>> roadsConnections,
            int connectionsCount,
            List<Double> carFlowPerMinute,
            int lightsCount,
            int timeUnitsInMinute,
            int numberOfTimeUnits,
            List<TrafficLightType> lightsType
    ) {
        this.optimizationTime = optimizationTime;
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
    }

    public int getOptimizationTime() {
        return optimizationTime;
    }

    public void setOptimizationTime(int optimizationTime) {
        this.optimizationTime = optimizationTime;
    }

    public int getRoadsCount() {
        return roadsCount;
    }

    public void setRoadsCount(int roadsCount) {
        this.roadsCount = roadsCount;
    }

    public List<Pair<Integer, Integer>> getLightCollisions() {
        return lightCollisions;
    }

    public void setLightCollisions(List<Pair<Integer, Integer>> lightCollisions) {
        this.lightCollisions = lightCollisions;
    }

    public int getLightCollisionsCount() {
        return lightCollisionsCount;
    }

    public void setLightCollisionsCount(int lightCollisionsCount) {
        this.lightCollisionsCount = lightCollisionsCount;
    }

    public List<Pair<Integer, Integer>> getHeavyCollisions() {
        return heavyCollisions;
    }

    public void setHeavyCollisions(List<Pair<Integer, Integer>> heavyCollisions) {
        this.heavyCollisions = heavyCollisions;
    }

    public int getHeavyCollisionsCount() {
        return heavyCollisionsCount;
    }

    public void setHeavyCollisionsCount(int heavyCollisionsCount) {
        this.heavyCollisionsCount = heavyCollisionsCount;
    }

    public List<List<Integer>> getRoadsConnections() {
        return roadsConnections;
    }

    public void setRoadsConnections(List<List<Integer>> roadsConnections) {
        this.roadsConnections = roadsConnections;
    }

    public int getConnectionsCount() {
        return connectionsCount;
    }

    public void setConnectionsCount(int connectionsCount) {
        this.connectionsCount = connectionsCount;
    }

    public List<Double> getCarFlowPerMinute() {
        return carFlowPerMinute;
    }

    public void setCarFlowPerMinute(List<Double> carFlowPerMinute) {
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

    public List<TrafficLightType> getLightsType() {
        return lightsType;
    }

    public void setLightsType(List<TrafficLightType> lightsType) {
        this.lightsType = lightsType;
    }
}
