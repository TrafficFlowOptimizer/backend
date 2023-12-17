package app.backend.response.optimization;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;

import java.util.HashMap;
import java.util.List;

public class OptimizationResultResponse {

    private HashMap<Integer, List<Integer>> lightsSequenceMapCurrent;
    private HashMap<Integer, Double> connectionsFlowRatioMapCurrent;
    private HashMap<Integer, List<Integer>> lightsSequenceMapPrevious;
    private HashMap<Integer, Double> connectionsFlowRatioMapPrevious;
    private HashMap<Integer, List<TrafficLight>> connectionsLightsMap;
    private HashMap<Integer, List<TrafficLight>> roadsLightsMap;
    private HashMap<Integer, TrafficLightDirection> lightsDirectionMap;
    private HashMap<Integer, Integer> connectionsFlowMap;
    private HashMap<Integer, Integer> connectionsRoadMap;
    private HashMap<Integer, Double> roadsFlowMap;
    private HashMap<Integer, Double> connectionChanceToPickMap;

    public OptimizationResultResponse(HashMap<Integer, List<Integer>> lightsSequenceMapCurrent,
                                      HashMap<Integer, Double> connectionsFlowRatioMapCurrent,
                                      HashMap<Integer, List<Integer>> lightsSequenceMapPrevious,
                                      HashMap<Integer, Double> connectionsFlowRatioMapPrevious,
                                      HashMap<Integer, List<TrafficLight>> connectionsLightsMap,
                                      HashMap<Integer, List<TrafficLight>> roadsLightsMap,
                                      HashMap<Integer, TrafficLightDirection> lightsDirectionMap,
                                      HashMap<Integer, Integer> connectionsFlowMap,
                                      HashMap<Integer, Integer> connectionsRoadMap,
                                      HashMap<Integer, Double> roadsFlowMap,
                                      HashMap<Integer, Double> connectionChanceToPickMap) {
        this.lightsSequenceMapCurrent = lightsSequenceMapCurrent;
        this.connectionsFlowRatioMapCurrent = connectionsFlowRatioMapCurrent;
        this.lightsSequenceMapPrevious = lightsSequenceMapPrevious;
        this.connectionsFlowRatioMapPrevious = connectionsFlowRatioMapPrevious;
        this.connectionsLightsMap = connectionsLightsMap;
        this.roadsLightsMap = roadsLightsMap;
        this.lightsDirectionMap = lightsDirectionMap;
        this.connectionsFlowMap = connectionsFlowMap;
        this.connectionsRoadMap = connectionsRoadMap;
        this.roadsFlowMap = roadsFlowMap;
        this.connectionChanceToPickMap = connectionChanceToPickMap;
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

    public HashMap<Integer, TrafficLightDirection> getLightsDirectionMap() {
        return lightsDirectionMap;
    }

    public void setLightsDirectionMap(HashMap<Integer, TrafficLightDirection> lightsDirectionMap) {
        this.lightsDirectionMap = lightsDirectionMap;
    }

    public HashMap<Integer, Integer> getConnectionsFlowMap() {
        return connectionsFlowMap;
    }

    public void setConnectionsFlowMap(HashMap<Integer, Integer> connectionsFlowMap) {
        this.connectionsFlowMap = connectionsFlowMap;
    }

    public HashMap<Integer, Integer> getConnectionsRoadMap() {
        return connectionsRoadMap;
    }

    public void setConnectionsRoadMap(HashMap<Integer, Integer> connectionsRoadMap) {
        this.connectionsRoadMap = connectionsRoadMap;
    }

    public HashMap<Integer, Double> getRoadsFlowMap() {
        return roadsFlowMap;
    }

    public void setRoadsFlowMap(HashMap<Integer, Double> roadsFlowMap) {
        this.roadsFlowMap = roadsFlowMap;
    }

    public HashMap<Integer, Double> getConnectionChanceToPickMap() {
        return connectionChanceToPickMap;
    }

    public void setConnectionChanceToPickMap(HashMap<Integer, Double> connectionChanceToPickMap) {
        this.connectionChanceToPickMap = connectionChanceToPickMap;
    }
}
