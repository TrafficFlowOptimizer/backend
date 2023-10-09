package app.backend.controller.crossroad;

import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import app.backend.entity.Video;
import app.backend.service.*;
import app.backend.service.VideoService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

        for(int i = 0; i<len1; i++) {
            JSONArray list = listOfLists.getJSONArray(i);
            int len2 = list.length();
            sequences.add(new ArrayList<>(len2));
            for(int j = 0; j<len2; j++) {
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

    public String parseOutput(List<List<Integer>> newestResult, List<List<Integer>> secondNewestResult, @PathVariable String crossroadId) throws Exception {
        Map<Integer, List<Integer>> resultsMap = new HashMap<>();
        for(int i=0; i<newestResult.size(); i++){
            resultsMap.put(i+1, newestResult.get(i));
        }
        Map<Integer, List<Integer>> previousResultsMap = new HashMap<>();
        for(int i=0; i<secondNewestResult.size(); i++){
            previousResultsMap.put(i+1, secondNewestResult.get(i));
        }

        Map<Integer, TrafficLightType> lightDirectionMap = new HashMap<>();
        for(int i=0; i<newestResult.size(); i++){
            int finalI = i;
            List<TrafficLightType> lightType = crossroadService
                    .getCrossroadById(crossroadId)
                    .getTrafficLightIds()
                    .stream()
                    .map(trafficLightService::getTrafficLightById)
                    .filter(light -> light.getIndex() == finalI + 1)
                    .map(TrafficLight::getType)
                    .toList();
            assert lightType.size() == 1;
            lightDirectionMap.put(i+1, lightType.get(0));
        }

        Crossroad crossroadDB = crossroadService.getCrossroadById(crossroadId);
        List<String> connectionsDB = crossroadDB.getConnectionIds();

        for(String connectionId : connectionsDB){
            List<Integer> innerList = new ArrayList<>();
            for(String trafficLightID : connectionService.getConnectionById(connectionId).getTrafficLightIds()) {
                innerList.add(trafficLightService.getTrafficLightById(trafficLightID).getIndex());
            }
            System.out.println(innerList);

            String carFlowID = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
            System.out.println(carFlowService.getCarFlowById(carFlowID).getCarFlow());
        }

        List<List<Integer>> roadConnections = connectionsDB
                .stream()
                .map(connectionId -> {
                    try {
                        List<Integer> innerList = new ArrayList<>();
                        for(String trafficLightID : connectionService.getConnectionById(connectionId).getTrafficLightIds()) {
                            innerList.add(trafficLightService.getTrafficLightById(trafficLightID).getIndex());
                        }
                        return innerList;
                    } catch (Exception e) {e.printStackTrace();}
                    return new ArrayList<Integer>();
                }).toList();

        List<? extends Number> flows = connectionsDB
                .stream()
                .map(connectionId -> {
                    try {
                        String carFlowID = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
                        return carFlowService.getCarFlowById(carFlowID).getCarFlow();
                    } catch (Exception e) {e.printStackTrace();}
                    return 1; // TODO: ?
                }).toList();

        JSONObject response = new JSONObject();
        JSONArray JsonConnections = new JSONArray();
        for(int i=0; i<roadConnections.size(); i++){
            JSONObject JsonConnection = new JSONObject();

            JSONArray JsonLights = new JSONArray();
            double possibleFlow = 0;
            double previousPossibleFlow = 0;
            for(int light:roadConnections.get(i)) {
                JSONObject JsonLight = new JSONObject();
                JsonLight.put("lightId", light);
                List<Integer> sequence = resultsMap.get(light);
                JsonLight.put("sequence", sequence);
                JsonLight.put("direction", lightDirectionMap.get(light));

                JsonLights.put(JsonLight);

                possibleFlow += countOccurrences(sequence, 1);
                previousPossibleFlow += countOccurrences(previousResultsMap.get(light), 1);
            }
            double expectedFlow = (double) flows.get(i);
            final DecimalFormat df = new DecimalFormat("0.00");
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            JsonConnection.put("currentFlow", df.format(possibleFlow / expectedFlow));

            JsonConnection.put("previousFlow", df.format(previousPossibleFlow / expectedFlow));

            JsonConnection.put("lights", JsonLights);

            JsonConnections.put(JsonConnection);
        }
        response.put("connections", JsonConnections);

        return response.toString();
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

    public JSONObject parseJSON(@PathVariable String crossroadId, @PathVariable int time) {
        JSONObject jsonBase = new JSONObject();
        jsonBase.put("time", time);

        JSONObject json = new JSONObject();

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

//  -----------------------------  roads  -----------------------------
            List<String> roads = crossroad.getRoadIds();
            json.put("number_of_roads", roads.size());

//  -----------------------------  collisions  -----------------------------
            List<String> collisions = crossroad.getCollisionIds();
            Map<Boolean, List<String>> collisionsDivided = collisions
                    .stream()
                    .collect(Collectors.partitioningBy(collisionId ->
                            collisionService.getCollisionById(collisionId).getType().equals(CollisionType.LIGHT)
                    ));

            List<String> lightCollisions = collisionsDivided.get(true);
            List<JSONArray> lightsLightCollisions = mapCollisions(lightCollisions);

            json.put("lights_light_collisions", lightsLightCollisions);
            json.put("light_collisions_no", lightsLightCollisions.size());


            List<String> heavyCollisions = collisionsDivided.get(false);
            List<JSONArray> lightsHeavyCollisions = mapCollisions(heavyCollisions);

            json.put("lights_heavy_collisions", lightsHeavyCollisions);
            json.put("heavy_collisions_no", lightsHeavyCollisions.size());

//  -----------------------------  connections  -----------------------------
            List<String> connections = crossroad.getConnectionIds();
            List<JSONArray> roadConnections = connections
                    .stream()
                    .map(connectionId -> {
                        try {
                            return new JSONArray(
                                    Arrays.asList(
                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getSourceId()).getIndex(),
                                            roadService.getRoadById(connectionService.getConnectionById(connectionId).getTargetId()).getIndex(),
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 0
                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(0)).getIndex() :
                                                    -1,
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 1
                                                    ? trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(1)).getIndex():
                                                    -1
                                    )
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new JSONArray();
                    }).toList();

            json.put("roads_connections", roadConnections);
            json.put("number_of_connections", roadConnections.size());

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

            json.put("car_flow_per_min", carFlows);

//  -----------------------------  lights  -----------------------------
            List<String> lights = crossroad.getTrafficLightIds();
            int numberOfLights = lights.size();

            json.put("number_of_lights", numberOfLights);

//  -----------------------------  fixed values  -----------------------------
            json.put("time_units_in_minute", 60); // fixed for now
            json.put("number_of_time_units", 60); // fixed for now

            json.put("lights_type", new JSONArray()); // optional
            json.put("lights", new JSONArray()); // optional

        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonBase.put("configuration", json);

        return jsonBase;
    }

    public List<JSONArray> mapCollisions(List<String> collisions) {
        return collisions
                .stream()
                .map(collisionId -> {
                    try {
                        return new JSONArray(
                                Arrays.asList(
                                        trafficLightService.getTrafficLightById(collisionService.getCollisionById(collisionId).getTrafficLight1Id()).getIndex(),
                                        trafficLightService.getTrafficLightById(collisionService.getCollisionById(collisionId).getTrafficLight2Id()).getIndex()
                                )
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new JSONArray();
                }).toList();
    }
}
