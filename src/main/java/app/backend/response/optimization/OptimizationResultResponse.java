package app.backend.response.optimization;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

public class OptimizationResultResponse {

    private HashMap<Integer, List<Integer>> lightsSequenceMapCurrent;
    private HashMap<Integer, Double> connectionsFlowRatioMapCurrent;
    @Nullable
    private HashMap<Integer, List<Integer>> lightsSequenceMapPrevious;
    @Nullable
    private HashMap<Integer, Double> connectionsFlowRatioMapPrevious;
    private HashMap<Integer, List<TrafficLight>> connectionsLightsMap;
    private HashMap<Integer, List<TrafficLight>> roadsLightsMap;
    private HashMap<Integer, TrafficLightType> lightsDirectionMap;

    public OptimizationResultResponse(HashMap<Integer, List<Integer>> lightsSequenceMapCurrent,
                                      HashMap<Integer, Double> connectionsFlowRatioMapCurrent,
                                      HashMap<Integer, List<Integer>> lightsSequenceMapPrevious,
                                      HashMap<Integer, Double> connectionsFlowRatioMapPrevious,
                                      HashMap<Integer, List<TrafficLight>> connectionsLightsMap,
                                      HashMap<Integer, List<TrafficLight>> roadsLightsMap,
                                      HashMap<Integer, TrafficLightType> lightsDirectionMap) {
        this.lightsSequenceMapCurrent = lightsSequenceMapCurrent;
        this.connectionsFlowRatioMapCurrent = connectionsFlowRatioMapCurrent;
        this.lightsSequenceMapPrevious = lightsSequenceMapPrevious;
        this.connectionsFlowRatioMapPrevious = connectionsFlowRatioMapPrevious;
        this.connectionsLightsMap = connectionsLightsMap;
        this.roadsLightsMap = roadsLightsMap;
        this.lightsDirectionMap = lightsDirectionMap;
    }

    public HashMap<Integer, List<Integer>> getLightsSequenceMapCurrent() {
        return lightsSequenceMapCurrent;
    }

    public void setLightsSequenceMapCurrent(HashMap<Integer, List<Integer>> lightsSequenceMapCurrent) {
        this.lightsSequenceMapCurrent = lightsSequenceMapCurrent;
    }

    public HashMap<Integer, Double> getConnectionsFlowRatioMapCurrent() {
        return connectionsFlowRatioMapCurrent;
    }

    public void setConnectionsFlowRatioMapCurrent(HashMap<Integer, Double> connectionsFlowRatioMapCurrent) {
        this.connectionsFlowRatioMapCurrent = connectionsFlowRatioMapCurrent;
    }

    public HashMap<Integer, List<Integer>> getLightsSequenceMapPrevious() {
        return lightsSequenceMapPrevious;
    }

    public void setLightsSequenceMapPrevious(HashMap<Integer, List<Integer>> lightsSequenceMapPrevious) {
        this.lightsSequenceMapPrevious = lightsSequenceMapPrevious;
    }

    public HashMap<Integer, Double> getConnectionsFlowRatioMapPrevious() {
        return connectionsFlowRatioMapPrevious;
    }

    public void setConnectionsFlowRatioMapPrevious(HashMap<Integer, Double> connectionsFlowRatioMapPrevious) {
        this.connectionsFlowRatioMapPrevious = connectionsFlowRatioMapPrevious;
    }

    public HashMap<Integer, List<TrafficLight>> getConnectionsLightsMap() {
        return connectionsLightsMap;
    }

    public void setConnectionsLightsMap(HashMap<Integer, List<TrafficLight>> connectionsLightsMap) {
        this.connectionsLightsMap = connectionsLightsMap;
    }

    public HashMap<Integer, List<TrafficLight>> getRoadsLightsMap() {
        return roadsLightsMap;
    }

    public void setRoadsLightsMap(HashMap<Integer, List<TrafficLight>> roadsLightsMap) {
        this.roadsLightsMap = roadsLightsMap;
    }

    public HashMap<Integer, TrafficLightType> getLightsDirectionMap() {
        return lightsDirectionMap;
    }

    public void setLightsDirectionMap(HashMap<Integer, TrafficLightType> lightsDirectionMap) {
        this.lightsDirectionMap = lightsDirectionMap;
    }
}
