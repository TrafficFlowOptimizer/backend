package app.backend.controller.optimization;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    public OptimizationRequest getOptimizationRequest(String crossroadId, String startTimeId, int time) { //TODO: check if light/connection order is preserved
        OptimizationRequest optimizationRequest = new OptimizationRequest();

        optimizationRequest.setOptimizationTime(time);

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

            if (crossroad == null) {
                throw new EntityNotFoundException();
            }

            //  -----------------------------  roads  -----------------------------
            List<Integer> roads = crossroad.getRoadIds().stream().map(roadId -> roadService.getRoadById(roadId).getIndex()).toList();
            optimizationRequest.setRoadsCount(roads.size());

            //  -----------------------------  collisions  -----------------------------
            Map<Boolean, List<Pair<Integer, Integer>>> collisionsPartitioned = crossroad
                    .getCollisionIds()
                    .stream()
                    .map(collisionService::getCollisionById)
                    .collect(
                            Collectors.partitioningBy(
                                    Collision::getBothCanBeOn,
                                    Collectors.flatMapping(collision -> {
                                                String connection1Id = collision.getConnection1Id();
                                                String connection2Id = collision.getConnection2Id();

                                                Connection connection1 = connectionService.getConnectionById(connection1Id);
                                                Connection connection2 = connectionService.getConnectionById(connection2Id);

                                                List<Pair<Integer, Integer>> lights = new LinkedList<>();
                                                for (String trafficLight1Id : connection1.getTrafficLightIds()) {
                                                    for (String trafficLight2Id : connection2.getTrafficLightIds()) {
                                                        lights.add(
                                                                Pair.of(
                                                                        trafficLightService.getTrafficLightById(trafficLight1Id).getIndex(),
                                                                        trafficLightService.getTrafficLightById(trafficLight2Id).getIndex()
                                                                )
                                                        );
                                                    }
                                                }
                                                return lights.stream();
                                            },
                                            Collectors.toList())
                            )
                    );
            List<List<Integer>> lightCollisions = collisionsPartitioned.get(true)
                    .stream()
                    .map(pair -> Arrays.asList(pair.getFirst(), pair.getSecond()))
                    .toList();
            optimizationRequest.setLightCollisions(lightCollisions);
            optimizationRequest.setLightCollisionsCount(lightCollisions.size());

            List<List<Integer>> heavyCollisions = collisionsPartitioned.get(false)
                    .stream()
                    .map(pair -> Arrays.asList(pair.getFirst(), pair.getSecond()))
                    .toList();
            optimizationRequest.setHeavyCollisions(heavyCollisions);
            optimizationRequest.setHeavyCollisionsCount(heavyCollisions.size());

            //  ------------------------  road connections lights  ------------------------
            List<String> connections = crossroad.getConnectionIds();
            List<List<Integer>> roadsConnectionsLights = connections
                    .stream()
                    .map(connectionId ->
                            Arrays.asList(
                                    roadService.getRoadById(connectionService.getConnectionById(connectionId).getSourceId()).getIndex(),
                                    roadService.getRoadById(connectionService.getConnectionById(connectionId).getTargetId()).getIndex(),
                                    connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 0
                                            ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(0)).getIndex() :
                                            -1,
                                    connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 1
                                            ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(1)).getIndex() :
                                            -1
                            )
                    ).toList();

            optimizationRequest.setRoadsConnectionsLights(roadsConnectionsLights);
            optimizationRequest.setConnectionsCount(roadsConnectionsLights.size());

            //  -----------------------------  car flow  -----------------------------
            List<Double> carFlows = connections
                    .stream()
                    .map(connectionId -> {
                        try {
                            return carFlowService.getNewestCarFlowByStartTimeIdForConnection(connectionId, startTimeId).getCarFlow();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).toList();

            optimizationRequest.setCarFlowPerMinute(carFlows);

            //  -----------------------------  lights  -----------------------------
            List<String> lights = crossroad.getTrafficLightIds();

            optimizationRequest.setLightsCount(lights.size());

            //  -----------------------------  connections  -----------------------------
            HashMap<String, List<String>> roadsMap = new HashMap<>();
            connections
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection -> {
                        List<String> currentSources = roadsMap.get(connection.getSourceId());
                        if (currentSources == null) {
                            roadsMap.put(connection.getSourceId(), Collections.singletonList(connection.getId()));
                        } else {
                            List<String> newSources = new ArrayList<>(currentSources);
                            newSources.add(connection.getId());
                            roadsMap.put(connection.getSourceId(), newSources);
                        }
                    });

            List<List<Integer>> connections_ = new ArrayList<>();
            for (int i = 0; i < crossroad.getRoadIds().size(); i++) {
                connections_.add(new ArrayList<>());
            }

            int maxConnectionsFromOneEntrance = 0;
            for (String sourceId : roadsMap.keySet()) {
                maxConnectionsFromOneEntrance = Math.max(maxConnectionsFromOneEntrance, roadsMap.get(sourceId).size());
                for (String connectionId : roadsMap.get(sourceId)) {
                    connections_
                            .get(roadService.getRoadById(sourceId).getIndex() - 1)
                            .add(connectionService.getConnectionById(connectionId).getIndex());
                }
            }
            for (List<Integer> sources : connections_) {
                while (sources.size() < maxConnectionsFromOneEntrance) {
                    sources.add(-1);
                }
            }
            optimizationRequest.setConnections(connections_);

            optimizationRequest.setMaxConnectionsFromOneEntrance(maxConnectionsFromOneEntrance);

            //  -----------------------------  intermediate capacities  -----------------------------

            List<List<Integer>> intermediatesCapacities = new ArrayList<>();

            //TODO

            optimizationRequest.setintermediatesCapacities(intermediatesCapacities);
            optimizationRequest.setintermediatesCount(intermediatesCapacities.size());

            //  -----------------------------  fixed values  -----------------------------

            optimizationRequest.setTimeUnitsInMinute(60);
            optimizationRequest.setNumberOfTimeUnits(60);
            optimizationRequest.setScaling(3);

            List<TrafficLightDirection> setLightsType = crossroad.getTrafficLightIds().stream()
                    .map(trafficLightService::getTrafficLightById)
                    .map(TrafficLight::getDirection)
                    .toList();
            optimizationRequest.setLightsType(setLightsType);

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
        int lightsCount = crossroad.getTrafficLightIds().size();
        for (int i = 0; i < lightsCount; i++) {
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
        LinkedHashMap<String, List<Integer>> mapping
                = (LinkedHashMap<String, List<Integer>>) new ObjectMapper().readValue(result.getBody(), HashMap.class)
                .get("lights_sequences");
        List<List<Integer>> sequences = new ArrayList<>();
        for (int i = 1; i <= mapping.size(); i++) {
            sequences.add(mapping.get(String.valueOf(i)));
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                sequences
        );
    }
}
