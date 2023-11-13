package app.backend.controller.optimization;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
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


    public OptimizationRequest getOptimizationRequest(String crossroadId, int time) { //TODO: check if light/connection order is preserved
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

    public void mockResponseToDb(String crossroadId, String startTimeId){
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        for(int i=0;i<crossroad.getTrafficLightIds().size();i++){
            List<Integer> list = new ArrayList<>();
            for(int j=0;j<60;j++){
                list.add((j+10*i)%60 < 30 ? 0 : 1);
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
        for(int i=0;i<mapping.size();i++){
            String sequenceAsString = mapping.get(String.valueOf(i+1)).toString();
            List<Integer> list = new ArrayList<>();
            for(int idx=1;idx<sequenceAsString.length();idx+=3){
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


    public static int countOccurrences(List<Integer> list, int target) throws JSONException {
        int count = 0;
        for (Integer integer : list) {
            if (integer == target) {
                count++;
            }
        }
        return count;
    }

//    public JSONObject getJsonData(String crossroadId, int time) { //TODO: check if all functionalities have been moved
//        JSONObject jsonBase = new JSONObject();
//        jsonBase.put("time", time);
//
//        JSONObject json = new JSONObject();
//        try {
//            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
//
////  -----------------------------  roads  -----------------------------
//            List<String> roads = crossroad.getRoadIds();
//            json.put("number_of_roads", roads.size());
//
////  -----------------------------  collisions  -----------------------------
//            Map<Boolean, List<Pair<Integer, Integer>>> collisionsPartitioned = crossroad
//                    .getCollisionIds()
//                    .stream()
//                    .map(collisionService::getCollisionById)
//                    .collect(
//                            Collectors.partitioningBy(
//                                    Collision::getBothCanBeOn,
//                                    Collectors.flatMapping(collision -> {
//                                                String connection1Id = collision.getConnection1Id();
//                                                String connection2Id = collision.getConnection2Id();
//
//                                                Connection connection1 = connectionService.getConnectionById(connection1Id);
//                                                Connection connection2 = connectionService.getConnectionById(connection2Id);
//
//                                                List<Pair<Integer, Integer>> lights = new LinkedList<>();
//                                                for (String trafficLight1Id : connection1.getTrafficLightIds()) {
//                                                    for (String trafficLight2Id : connection2.getTrafficLightIds()) {
//                                                        lights.add(
//                                                                Pair.of(
//                                                                        trafficLightService.getTrafficLightById(trafficLight1Id).getIndex(),
//                                                                        trafficLightService.getTrafficLightById(trafficLight2Id).getIndex()
//                                                                )
//                                                        );
//                                                    }
//                                                }
//                                                return lights.stream();
//                                            },
//                                            Collectors.toList())
//                            )
//                    );
//
//            json.put("lights_light_collisions", collisionsPartitioned.get(true));
//            json.put("light_collisions_no", collisionsPartitioned.get(true).size());
//
//            json.put("lights_heavy_collisions", collisionsPartitioned.get(false));
//            json.put("heavy_collisions_no", collisionsPartitioned.get(false).size());
//
////  -----------------------------  connections  -----------------------------
//            List<String> connections = crossroad.getConnectionIds();
//            List<JSONArray> roadConnections = connections
//                    .stream()
//                    .map(connectionId -> {
//                        try {
//                            return new JSONArray(
//                                    Arrays.asList(
//                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getSourceId()).getIndex(),
//                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getTargetId()).getIndex(),
//                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 0
//                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(0)).getIndex() :
//                                                    -1,
//                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 1
//                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(1)).getIndex() :
//                                                    -1
//                                    )
//                            );
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return new JSONArray();
//                    }).toList();
//
//            json.put("roads_connections", roadConnections);
//            json.put("number_of_connections", roadConnections.size());
//
////  -----------------------------  car flow  -----------------------------
//            List<Double> carFlows = connections
//                    .stream()
//                    .map(connectionId -> {
//                        try {
//                            String sampleCarFlowId = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
//                            return carFlowService.getCarFlowById(sampleCarFlowId).getCarFlow();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }).toList();
//
//            json.put("car_flow_per_min", carFlows);
//
////  -----------------------------  lights  -----------------------------
//            List<String> lights = crossroad.getTrafficLightIds();
//            int numberOfLights = lights.size();
//
//            json.put("number_of_lights", numberOfLights);
//
////  -----------------------------  fixed values  -----------------------------
//            json.put("time_units_in_minute", 60); // fixed for now
//            json.put("number_of_time_units", 60); // fixed for now
//
//            json.put("lights_type", new JSONArray()); // optional
//            json.put("lights", new JSONArray()); // optional
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        jsonBase.put("configuration", json);
//
//        return jsonBase;
//    }

//    public String parseOutput(List<List<Integer>> newestResult, List<List<Integer>> secondNewestResult, String crossroadId) {
//        Map<Integer, List<Integer>> resultsMap = new HashMap<>();
//        for (int i = 0; i < newestResult.size(); i++) {
//            resultsMap.put(i + 1, newestResult.get(i));
//        }
//        Map<Integer, List<Integer>> previousResultsMap = new HashMap<>();
//        for (int i = 0; i < secondNewestResult.size(); i++) {
//            previousResultsMap.put(i + 1, secondNewestResult.get(i));
//        }
//
//        Map<Integer, TrafficLightType> lightDirectionMap = new HashMap<>();
//        for (int i = 0; i < newestResult.size(); i++) {
//            int finalI = i;
//            List<TrafficLightType> lightType = crossroadService
//                    .getCrossroadById(crossroadId)
//                    .getTrafficLightIds()
//                    .stream()
//                    .map(trafficLightService::getTrafficLightById)
//                    .filter(light -> light.getIndex() == finalI + 1)
//                    .map(TrafficLight::getDirection)
//                    .toList();
//            assert lightType.size() == 1;
//            lightDirectionMap.put(i + 1, lightType.get(0));
//        }
//
//        Crossroad crossroadDB = crossroadService.getCrossroadById(crossroadId);
//        List<String> connectionsDB = crossroadDB.getConnectionIds();
//
//        for (String connectionId : connectionsDB) {
//            List<Integer> innerList = new ArrayList<>();
//            for (String trafficLightID : connectionService.getConnectionById(connectionId).getTrafficLightIds()) {
//                innerList.add(trafficLightService.getTrafficLightById(trafficLightID).getIndex());
//            }
//            System.out.println(innerList);
//
//            String carFlowID = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
//            System.out.println(carFlowService.getCarFlowById(carFlowID).getCarFlow());
//        }
//
//        List<List<Integer>> roadConnections = connectionsDB
//                .stream()
//                .map(connectionId -> {
//                    try {
//                        List<Integer> innerList = new ArrayList<>();
//                        for (String trafficLightID : connectionService.getConnectionById(connectionId).getTrafficLightIds()) {
//                            innerList.add(trafficLightService.getTrafficLightById(trafficLightID).getIndex());
//                        }
//                        return innerList;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return new ArrayList<Integer>();
//                }).toList();
//
//        List<? extends Number> flows = connectionsDB
//                .stream()
//                .map(connectionId -> {
//                    try {
//                        String carFlowID = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
//                        return carFlowService.getCarFlowById(carFlowID).getCarFlow();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return 1; // TODO: ?
//                }).toList();
//
//        JSONObject response = new JSONObject();
//        JSONArray JsonConnections = new JSONArray();
//        for (int i = 0; i < roadConnections.size(); i++) {
//            JSONObject JsonConnection = new JSONObject();
//
//            JSONArray JsonLights = new JSONArray();
//            double possibleFlow = 0;
//            double previousPossibleFlow = 0;
//            for (int light : roadConnections.get(i)) {
//                JSONObject JsonLight = new JSONObject();
//                JsonLight.put("lightId", light);
//                List<Integer> sequence = resultsMap.get(light);
//                JsonLight.put("sequence", sequence);
//                JsonLight.put("direction", lightDirectionMap.get(light));
//
//                JsonLights.put(JsonLight);
//
//                possibleFlow += countOccurrences(sequence, 1);
//                previousPossibleFlow += countOccurrences(previousResultsMap.get(light), 1);
//            }
//            double expectedFlow = (double) flows.get(i);
//            final DecimalFormat df = new DecimalFormat("0.00");
//            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
//            JsonConnection.put("currentFlow", df.format(possibleFlow / expectedFlow));
//
//            JsonConnection.put("previousFlow", df.format(previousPossibleFlow / expectedFlow));
//
//            JsonConnection.put("lights", JsonLights);
//
//            JsonConnections.put(JsonConnection);
//        }
//        response.put("connections", JsonConnections);
//
//        return response.toString();
//    }
}
