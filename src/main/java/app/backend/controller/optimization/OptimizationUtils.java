package app.backend.controller.optimization;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.*;
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

            optimizationRequest.setLightCollisions(collisionsPartitioned.get(true));
            optimizationRequest.setLightCollisionsCount(collisionsPartitioned.get(true).size());
            optimizationRequest.setHeavyCollisions(collisionsPartitioned.get(false));
            optimizationRequest.setHeavyCollisionsCount(collisionsPartitioned.get(false).size());

            //  -----------------------------  connections  -----------------------------
            List<String> connections = crossroad.getConnectionIds();
            List<List<Integer>> roadConnections = connections
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

            optimizationRequest.setRoadsConnections(roadConnections);
            optimizationRequest.setConnectionsCount(roadConnections.size());

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

            //  -----------------------------  fixed values  -----------------------------

            optimizationRequest.setTimeUnitsInMinute(60);
            optimizationRequest.setNumberOfTimeUnits(60);

            List<TrafficLightType> setLightsType = crossroad.getTrafficLightIds().stream()
                    .map(trafficLightService::getTrafficLightById)
                    .map(TrafficLight::getDirection)
                    .toList();
            optimizationRequest.setLightsType(setLightsType);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimizationRequest;
    }

    public void mockResponseToDb(String crossroadId, String startTimeId) {
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        for (int i = 0; i < crossroad.getTrafficLightIds().size(); i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 60; j++) {
                list.add((j + 10 * i) % 60 < 30 ? 0 : 1);
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
        HashMap<String, Object> mapping = new ObjectMapper().readValue(result.getBody(), HashMap.class);

        List<List<Integer>> sequences = new ArrayList<>(mapping.size());
        for (int i = 0; i < mapping.size(); i++) {
            String sequenceAsString = mapping.get(String.valueOf(i + 1)).toString();
            List<Integer> list = new ArrayList<>();
            for (int idx = 1; idx < sequenceAsString.length(); idx += 3) {
                list.add(Integer.parseInt(String.valueOf(sequenceAsString.charAt(idx))));
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
}
