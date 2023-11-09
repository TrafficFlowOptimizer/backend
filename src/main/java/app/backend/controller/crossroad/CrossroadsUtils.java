package app.backend.controller.crossroad;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.Video;
import app.backend.document.crossroad.Crossroad;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class CrossroadsUtils {
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final TrafficLightService trafficLightService;
    private final ConnectionService connectionService;
    private final CarFlowService carFlowService;
    private final OptimizationService optimizationService;
    private final VideoService videoService;

    @Autowired
    public CrossroadsUtils(
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

    public String getTimeIntervalId(String videoId) {
        Video video = videoService.getVideo(videoId);
        if (video == null) {
            return null;
        }

        return video.getTimeIntervalId();
    }

    public void addOptimizationResultsToDb(String crossroadId, String timeIntervalId, String results) {
        JSONObject res = new JSONObject(results);
        JSONArray listOfLists = res.getJSONArray("results");
        int len1 = listOfLists.length();

        List<List<Integer>> sequences = new ArrayList<>(len1);

        for (int i = 0; i < len1; i++) {
            JSONArray list = listOfLists.getJSONArray(i);
            int len2 = list.length();
            sequences.add(new ArrayList<>(len2));
            for (int j = 0; j < len2; j++) {
                sequences.get(i).add(list.getInt(j));
            }
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                timeIntervalId,
                sequences
        );
    }




    public OptimizationRequest getOptimizationRequest(String crossroadId, int time) {
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
                    .map(connectionId -> {
                        try {
                            return Arrays.asList(
                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getSourceId()).getIndex(),
                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getTargetId()).getIndex(),
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 0
                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(0)).getIndex() :
                                                    -1,
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 1
                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(1)).getIndex() :
                                                    -1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Arrays.asList(0, 0, 0, 0);
                    }).toList();

            optimizationRequest.setRoadsConnections(roadConnections);
            optimizationRequest.setConnectionsCount(roadConnections.size());

            //  -----------------------------  car flow  -----------------------------
            List<Double> carFlows = connections
                    .stream()
                    .map(connectionId -> {
                        try {
                            String sampleCarFlowId = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
                            return carFlowService.getCarFlowById(sampleCarFlowId).getCarFlow();
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

            optimizationRequest.setLightsType(new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimizationRequest;
    }
}
