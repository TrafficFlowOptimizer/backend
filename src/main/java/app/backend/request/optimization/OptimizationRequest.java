package app.backend.request.optimization;

import app.backend.document.light.TrafficLightDirection;

import java.util.ArrayList;
import java.util.List;

public class OptimizationRequest {
    private int optimizationTime;
    private int scaling;
    private List<TrafficLightDirection> lightsTypes;

    private int timeUnitsInMinute;
    private int timeUnitCount;
    private int lightCount;
    private int roadCount;
    private int connectionCount;
    private int collisionCount;

    private List<Integer> roadCapacities;
    private List<Integer> expectedCarFlow;
    private List<List<Integer>> connectionLights;
    private List<List<Integer>> roadConnectionsIn;
    private List<List<Integer>> roadConnectionsOut;
    private List<Integer> isCollisionImportant;
    private List<List<Integer>> collisionConnections;
    private List<Integer> isConnectionFromIntermediate;

    private List<List<Integer>> previousResults;

    public OptimizationRequest() {
        this(0, 0, new ArrayList<>(), 0, 0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public OptimizationRequest(
            int optimizationTime,
            int scaling,
            List<TrafficLightDirection> lightsTypes,
            int timeUnitsInMinute,
            int timeUnitCount,
            int lightCount,
            int roadCount,
            int connectionCount,
            int collisionCount,
            List<Integer> roadCapacities,
            List<Integer> expectedCarFlow,
            List<List<Integer>> connectionLights,
            List<List<Integer>> roadConnectionsIn,
            List<List<Integer>> roadConnectionsOut,
            List<Integer> isCollisionImportant,
            List<List<Integer>> collisionConnections,
            List<Integer> isConnectionFromIntermediate,
            List<List<Integer>> previousResults
    ) {
        this.optimizationTime = optimizationTime;
        this.scaling = scaling;
        this.lightsTypes = lightsTypes;
        this.timeUnitsInMinute = timeUnitsInMinute;
        this.timeUnitCount = timeUnitCount;
        this.lightCount = lightCount;
        this.roadCount = roadCount;
        this.connectionCount = connectionCount;
        this.collisionCount = collisionCount;
        this.roadCapacities = roadCapacities;
        this.expectedCarFlow = expectedCarFlow;
        this.connectionLights = connectionLights;
        this.roadConnectionsIn = roadConnectionsIn;
        this.roadConnectionsOut = roadConnectionsOut;
        this.isCollisionImportant = isCollisionImportant;
        this.collisionConnections = collisionConnections;
        this.isConnectionFromIntermediate = isConnectionFromIntermediate;
        this.previousResults = previousResults;
    }

    public int getOptimizationTime() {
        return optimizationTime;
    }

    public void setOptimizationTime(int optimizationTime) {
        this.optimizationTime = optimizationTime;
    }

    public int getScaling() {
        return scaling;
    }

    public void setScaling(int scaling) {
        this.scaling = scaling;
    }

    public List<TrafficLightDirection> getLightsTypes() {
        return lightsTypes;
    }

    public void setLightsTypes(List<TrafficLightDirection> lightsTypes) {
        this.lightsTypes = lightsTypes;
    }

    public int getTimeUnitsInMinute() {
        return timeUnitsInMinute;
    }

    public void setTimeUnitsInMinute(int timeUnitsInMinute) {
        this.timeUnitsInMinute = timeUnitsInMinute;
    }

    public int getTimeUnitCount() {
        return timeUnitCount;
    }

    public void setTimeUnitCount(int timeUnitCount) {
        this.timeUnitCount = timeUnitCount;
    }

    public int getLightCount() {
        return lightCount;
    }

    public void setLightCount(int lightCount) {
        this.lightCount = lightCount;
    }

    public int getRoadCount() {
        return roadCount;
    }

    public void setRoadCount(int roadCount) {
        this.roadCount = roadCount;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public void setCollisionCount(int collisionCount) {
        this.collisionCount = collisionCount;
    }

    public List<Integer> getRoadCapacities() {
        return roadCapacities;
    }

    public void setRoadCapacities(List<Integer> roadCapacities) {
        this.roadCapacities = roadCapacities;
    }

    public List<Integer> getExpectedCarFlow() {
        return expectedCarFlow;
    }

    public void setExpectedCarFlow(List<Integer> expectedCarFlow) {
        this.expectedCarFlow = expectedCarFlow;
    }

    public List<List<Integer>> getConnectionLights() {
        return connectionLights;
    }

    public void setConnectionLights(List<List<Integer>> connectionLights) {
        this.connectionLights = connectionLights;
    }

    public List<List<Integer>> getRoadConnectionsIn() {
        return roadConnectionsIn;
    }

    public void setRoadConnectionsIn(List<List<Integer>> roadConnectionsIn) {
        this.roadConnectionsIn = roadConnectionsIn;
    }

    public List<List<Integer>> getRoadConnectionsOut() {
        return roadConnectionsOut;
    }

    public void setRoadConnectionsOut(List<List<Integer>> roadConnectionsOut) {
        this.roadConnectionsOut = roadConnectionsOut;
    }

    public List<Integer> getIsCollisionImportant() {
        return isCollisionImportant;
    }

    public void setIsCollisionImportant(List<Integer> isCollisionImportant) {
        this.isCollisionImportant = isCollisionImportant;
    }

    public List<List<Integer>> getCollisionConnections() {
        return collisionConnections;
    }

    public void setCollisionConnections(List<List<Integer>> collisionConnections) {
        this.collisionConnections = collisionConnections;
    }

    public List<Integer> getIsConnectionFromIntermediate() {
        return isConnectionFromIntermediate;
    }

    public void setIsConnectionFromIntermediate(List<Integer> isConnectionFromIntermediate) {
        this.isConnectionFromIntermediate = isConnectionFromIntermediate;
    }

    public List<List<Integer>> getPreviousResults() {
        return previousResults;
    }

    public void setPreviousResults(List<List<Integer>> previousResults) {
        this.previousResults = previousResults;
    }
}
