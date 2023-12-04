package app.backend.controller.optimization;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;
import app.backend.document.road.Road;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.service.CarFlowService;
import app.backend.service.CollisionService;
import app.backend.service.ConnectionService;
import app.backend.service.CrossroadService;
import app.backend.service.OptimizationService;
import app.backend.service.RoadService;
import app.backend.service.TrafficLightService;
import app.backend.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Component
public class OptimizationUtils {
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final TrafficLightService trafficLightService;
    private final ConnectionService connectionService;
    private final CarFlowService carFlowService;
    private final OptimizationService optimizationService;
    private final VideoService videoService;


    @Autowired
    public OptimizationUtils(
            CrossroadService crossroadService,
            RoadService roadService,
            CollisionService collisionService,
            TrafficLightService trafficLightService,
            ConnectionService connectionService,
            CarFlowService carFlowService,
            OptimizationService optimizationService,
            VideoService videoService
    ) {
        this.crossroadService = crossroadService;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.trafficLightService = trafficLightService;
        this.connectionService = connectionService;
        this.carFlowService = carFlowService;
        this.optimizationService = optimizationService;
        this.videoService = videoService;
    }

    public OptimizationRequest getOptimizationRequest(String crossroadId, String startTimeId, int time, int scaling) { //TODO: check if light/connection order is preserved
        OptimizationRequest optimizationRequest = new OptimizationRequest();

        optimizationRequest.setOptimizationTime(time);

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

            if (crossroad == null) {
                throw new EntityNotFoundException();
            }

            //  -----------------------------  roads  -----------------------------

            List<Integer> roadCapacities = crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById)
                    .sorted(Comparator.comparingInt(Road::getIndex))
                    .map(road -> {
                        return road.getCapacity() == -1 ? 0 : road.getCapacity();
                    })
                    .toList();

            optimizationRequest.setRoadCapacities(roadCapacities);
            optimizationRequest.setRoadCount(roadCapacities.size());

            //  -----------------------------  collisions  -----------------------------

            List<Integer> isCollisionImportant = crossroad
                    .getCollisionIds()
                    .stream()
                    .map(collisionService::getCollisionById)
                    .sorted(Comparator.comparingInt(Collision::getIndex))
                    .map(collision -> {
                        return collision.getBothCanBeOn() ? 0 : 1;

                    })
                    .toList();

            List<List<Integer>> CollisionConnections = crossroad
                    .getCollisionIds()
                    .stream()
                    .map(collisionService::getCollisionById)
                    .sorted(Comparator.comparingInt(Collision::getIndex))
                    .map(collision -> {
                        String connection1Id = collision.getConnection1Id();
                        String connection2Id = collision.getConnection2Id();

                        Connection connection1 = connectionService.getConnectionById(connection1Id);
                        Connection connection2 = connectionService.getConnectionById(connection2Id);
                        return Arrays.asList(connection1.getIndex(), connection2.getIndex());
                    })
                    .toList();

            optimizationRequest.setIsCollisionImportant(isCollisionImportant);
            optimizationRequest.setCollisionConnections(CollisionConnections);
            optimizationRequest.setCollisionCount(CollisionConnections.size());

            //  ------------------------  road connections lights  ------------------------

            List<String> connections = crossroad.getConnectionIds();
            List<Road> roads = crossroad.getRoadIds().stream().map(roadService::getRoadById).toList();

            HashMap<Integer, List<Integer>> roadConnectionsInMap = new HashMap<>();
            HashMap<Integer, List<Integer>> roadConnectionsOutMap = new HashMap<>();
            List<Integer> isConnectionFromIntermediate = new ArrayList<>();


            connections.stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .forEach(connection -> {
                        isConnectionFromIntermediate.add(roadService.getRoadById(connection.getSourceId()).getCapacity() == -1 ? 0 : 1);

                        int targetIdx = roadService.getRoadById(connection.getTargetId()).getIndex();
                        if (roadConnectionsInMap.containsKey(targetIdx)) {
                            List<Integer> currentConnections = new ArrayList<>();
                            currentConnections.addAll(roadConnectionsInMap.get(targetIdx));
                            currentConnections.add(connection.getIndex());
                            roadConnectionsInMap.put(targetIdx, currentConnections);
                        } else {
                            roadConnectionsInMap.put(targetIdx, Arrays.asList(connection.getIndex()));
                        }
                        int sourceIdx = roadService.getRoadById(connection.getSourceId()).getIndex();
                        if (roadConnectionsOutMap.containsKey(sourceIdx)) {
                            List<Integer> currentConnections = new ArrayList<>();
                            currentConnections.addAll(roadConnectionsOutMap.get(sourceIdx));
                            currentConnections.add(connection.getIndex());
                            roadConnectionsOutMap.put(sourceIdx, currentConnections);
                        } else {
                            roadConnectionsOutMap.put(sourceIdx, Arrays.asList(connection.getIndex()));
                        }
                    });

            List<List<Integer>> roadConnectionsIn = new ArrayList<>();
            for (Road road : roads) {
                List<Integer> newCons = new ArrayList<>();
                if (roadConnectionsInMap.containsKey(road.getIndex())) {
                    newCons = new ArrayList<>(roadConnectionsInMap.get(road.getIndex()));
                }
                while (newCons.size() < 3) {
                    newCons.add(0);
                }
                roadConnectionsIn.add(newCons);
            }

            List<List<Integer>> roadConnectionsOut = new ArrayList<>();
            for (Road road : roads) {
                List<Integer> newCons = new ArrayList<>();
                if (roadConnectionsOutMap.containsKey(road.getIndex())) {
                    newCons = new ArrayList<>(roadConnectionsOutMap.get(road.getIndex()));
                }
                while (newCons.size() < 3) {
                    newCons.add(0);
                }
                roadConnectionsOut.add(newCons);
            }

            Collections.reverse(isConnectionFromIntermediate);
            optimizationRequest.setIsConnectionFromIntermediate(isConnectionFromIntermediate);

            optimizationRequest.setRoadConnectionsIn(roadConnectionsIn);
            optimizationRequest.setRoadConnectionsOut(roadConnectionsOut);
            optimizationRequest.setConnectionCount(connections.size());

            //  -----------------------------  car flow  -----------------------------

            List<Integer> carFlows = new ArrayList<>(connections
                    .stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .map(connection -> {
                        try {//TODO: usunąć poniższe "(int)" po zmianie bazki
                            return (int) carFlowService.getNewestCarFlowByStartTimeIdForConnection(connection.getId(), startTimeId).getCarFlow();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).toList());

            Collections.reverse(carFlows);
            optimizationRequest.setExpectedCarFlow(carFlows);

            //  -----------------------------  lights  -----------------------------

            List<List<Integer>> connectionsLights = new ArrayList<>(connections
                    .stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .map(connection -> {
                        List<Integer> lights = new ArrayList<>(connection.getTrafficLightIds()
                                .stream()
                                .map(trafficLightService::getTrafficLightById)
                                .map(TrafficLight::getIndex)
                                .toList());
                        while (lights.size() < 3) {
                            lights.add(0);
                        }
                        return lights;
                    })
                    .toList());

            List<String> lights = crossroad.getTrafficLightIds();

            Collections.reverse(connectionsLights);
            optimizationRequest.setConnectionLights(connectionsLights);
            optimizationRequest.setLightCount(lights.size());

            //  -----------------------------  fixed values  -----------------------------

            optimizationRequest.setTimeUnitsInMinute(60);
            optimizationRequest.setTimeUnitCount(60);
            optimizationRequest.setScaling(scaling);

            List<TrafficLightDirection> setLightsType = crossroad.getTrafficLightIds().stream()
                    .map(trafficLightService::getTrafficLightById)
                    .map(TrafficLight::getDirection)
                    .toList();
            optimizationRequest.setLightsTypes(setLightsType);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimizationRequest;
    }

    public void mockResponseToDb(String crossroadId, String startTimeId, OptimizationResultMock mockVersion) {
        switch (mockVersion) {
            case RANDOM -> mockRandom(crossroadId, startTimeId);
            case LIGHT_BY_LIGHT -> mockLightByLight(crossroadId, startTimeId);
        }
    }

    private void mockRandom(String crossroadId, String startTimeId) {
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        for (int i = 0; i < crossroad.getTrafficLightIds().size(); i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 60; j++) {
                double random = Math.random();
                if (random < 0.25) {
                    list.add(1);
                } else {
                    list.add(0);
                }
            }
            sequences.add(list);
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                sequences
        );
    }

    private void mockLightByLight(String crossroadId, String startTimeId) {
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        int lightInterval = (int) Math.floor(60.0 / crossroad.getTrafficLightIds().size());
        int lightCount = crossroad.getTrafficLightIds().size();
        for (int i = 0; i < lightCount; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 60; j++) {
                if (lightInterval * i <= j && j < lightInterval * (i + 1)) {
                    list.add(1);
                } else {
                    list.add(0);
                }
            }
            sequences.add(list);
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                sequences
        );
    }

    public void addOptimizationResultsToDb(String crossroadId, String startTimeId, ResponseEntity<String> result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<Integer>> resultList = objectMapper.readValue(result.getBody(), new TypeReference<List<List<Integer>>>() {
        });

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                resultList
        );
    }
}
