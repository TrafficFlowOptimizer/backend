package app.backend.response.optimization;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

public class OptimizationResultResponse {

    private HashMap<String, List<Integer>> lightsSequenceMapCurrent;
    private HashMap<String, Double> connectionsFlowRatioMapCurrent;
    @Nullable
    private HashMap<String, List<Integer>> lightsSequenceMapPrevious;
    @Nullable
    private HashMap<String, Double> connectionsFlowRatioMapPrevious;
    private HashMap<String, List<TrafficLight>> connectionsLightsMap;
    private HashMap<String, TrafficLightType> lightsDirectionMap;

    public OptimizationResultResponse(HashMap<String, List<Integer>> lightsSequenceMapCurrent,
                                      HashMap<String, Double> connectionsFlowRatioMapCurrent,
                                      HashMap<String, List<Integer>> lightsSequenceMapPrevious,
                                      HashMap<String, Double> connectionsFlowRatioMapPrevious,
                                      HashMap<String, List<TrafficLight>> connectionsLightsMap,
                                      HashMap<String, TrafficLightType> lightsDirectionMap) {
        this.lightsSequenceMapCurrent = lightsSequenceMapCurrent;
        this.connectionsFlowRatioMapCurrent = connectionsFlowRatioMapCurrent;
        this.lightsSequenceMapPrevious = lightsSequenceMapPrevious;
        this.connectionsFlowRatioMapPrevious = connectionsFlowRatioMapPrevious;
        this.connectionsLightsMap = connectionsLightsMap;
        this.lightsDirectionMap = lightsDirectionMap;
    }

    public HashMap<String, List<Integer>> getLightsSequenceMapCurrent() {
        return lightsSequenceMapCurrent;
    }

    public void setLightsSequenceMapCurrent(HashMap<String, List<Integer>> lightsSequenceMapCurrent) {
        this.lightsSequenceMapCurrent = lightsSequenceMapCurrent;
    }

    public HashMap<String, Double> getConnectionsFlowRatioMapCurrent() {
        return connectionsFlowRatioMapCurrent;
    }

    public void setConnectionsFlowRatioMapCurrent(HashMap<String, Double> connectionsFlowRatioMapCurrent) {
        this.connectionsFlowRatioMapCurrent = connectionsFlowRatioMapCurrent;
    }

    public HashMap<String, List<Integer>> getLightsSequenceMapPrevious() {
        return lightsSequenceMapPrevious;
    }

    public void setLightsSequenceMapPrevious(HashMap<String, List<Integer>> lightsSequenceMapPrevious) {
        this.lightsSequenceMapPrevious = lightsSequenceMapPrevious;
    }

    public HashMap<String, Double> getConnectionsFlowRatioMapPrevious() {
        return connectionsFlowRatioMapPrevious;
    }

    public void setConnectionsFlowRatioMapPrevious(HashMap<String, Double> connectionsFlowRatioMapPrevious) {
        this.connectionsFlowRatioMapPrevious = connectionsFlowRatioMapPrevious;
    }

    public HashMap<String, List<TrafficLight>> getConnectionsLightsMap() {
        return connectionsLightsMap;
    }

    public void setConnectionsLightsMap(HashMap<String, List<TrafficLight>> connectionsLightsMap) {
        this.connectionsLightsMap = connectionsLightsMap;
    }

    public HashMap<String, TrafficLightType> getLightsDirectionMap() {
        return lightsDirectionMap;
    }

    public void setLightsDirectionMap(HashMap<String, TrafficLightType> lightsDirectionMap) {
        this.lightsDirectionMap = lightsDirectionMap;
    }
}
