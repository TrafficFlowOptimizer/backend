package app.backend.request.optimization;

import app.backend.document.light.TrafficLightType;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class OptimizationRequest {
    private int optimizationTime;
    private int roadsCount;
    private List<List<Integer>> lightCollisions;
    private int lightCollisionsCount;
    private List<List<Integer>> heavyCollisions;
    private int heavyCollisionsCount;
    private List<List<Integer>> roadsConnectionsLights;
    private int connectionsCount;
    private List<Double> carFlowPerMinute;
    private int lightsCount;
    private int timeUnitsInMinute;
    private int numberOfTimeUnits;
    private List<TrafficLightType> lightsType;
    private int maxConnectionsFromOneEntrance;
    private List<List<Integer>> connections;
    private int scaling;
    


    public OptimizationRequest() {
        this(0, 0, new ArrayList<>(), 0, new ArrayList<>(), 0, new ArrayList<>(), 0, new ArrayList<>(), 0, 0, 0, new ArrayList<>(), 0, new ArrayList<>(), 0);
    }

    public OptimizationRequest(
            int optimizationTime,
            int roadsCount,
            List<List<Integer>> lightCollisions,
            int lightCollisionsCount,
            List<List<Integer>> heavyCollisions,
            int heavyCollisionsCount,
            List<List<Integer>> roadsConnectionsLights,
            int connectionsCount,
            List<Double> carFlowPerMinute,
            int lightsCount,
            int timeUnitsInMinute,
            int numberOfTimeUnits,
            List<TrafficLightType> lightsType,
            int maxConnectionsFromOneEntrance,
            List<List<Integer>> connections,
            int scaling
    ) {
        this.optimizationTime = optimizationTime;
        this.roadsCount = roadsCount;
        this.lightCollisions = lightCollisions;
        this.lightCollisionsCount = lightCollisionsCount;
        this.heavyCollisions = heavyCollisions;
        this.heavyCollisionsCount = heavyCollisionsCount;
        this.roadsConnectionsLights = roadsConnectionsLights;
        this.connectionsCount = connectionsCount;
        this.carFlowPerMinute = carFlowPerMinute;
        this.lightsCount = lightsCount;
        this.timeUnitsInMinute = timeUnitsInMinute;
        this.numberOfTimeUnits = numberOfTimeUnits;
        this.lightsType = lightsType;
        this.maxConnectionsFromOneEntrance = maxConnectionsFromOneEntrance;
        this.connections = connections;
        this.scaling = scaling;
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

    public List<List<Integer>> getLightCollisions() {
        return lightCollisions;
    }

    public void setLightCollisions(List<List<Integer>> lightCollisions) {
        this.lightCollisions = lightCollisions;
    }

    public int getLightCollisionsCount() {
        return lightCollisionsCount;
    }

    public void setLightCollisionsCount(int lightCollisionsCount) {
        this.lightCollisionsCount = lightCollisionsCount;
    }

    public List<List<Integer>> getHeavyCollisions() {
        return heavyCollisions;
    }

    public void setHeavyCollisions(List<List<Integer>> heavyCollisions) {
        this.heavyCollisions = heavyCollisions;
    }

    public int getHeavyCollisionsCount() {
        return heavyCollisionsCount;
    }

    public void setHeavyCollisionsCount(int heavyCollisionsCount) {
        this.heavyCollisionsCount = heavyCollisionsCount;
    }

    public List<List<Integer>> getRoadsConnectionsLights() {
        return roadsConnectionsLights;
    }

    public void setRoadsConnectionsLights(List<List<Integer>> roadsConnectionsLights) {
        this.roadsConnectionsLights = roadsConnectionsLights;
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

    public int getMaxConnectionsFromOneEntrance() {
        return maxConnectionsFromOneEntrance;
    }

    public void setMaxConnectionsFromOneEntrance(int maxConnectionsFromOneEntrance) {
        this.maxConnectionsFromOneEntrance = maxConnectionsFromOneEntrance;
    }

    public List<List<Integer>> getConnections() {
        return connections;
    }

    public void setConnections(List<List<Integer>> connections) {
        this.connections = connections;
    }

    public int getScaling() {
        return scaling;
    }

    public void setScaling(int scaling) {
        this.scaling = scaling;
    }
}
