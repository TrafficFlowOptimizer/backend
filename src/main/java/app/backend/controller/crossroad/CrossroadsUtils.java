package app.backend.controller.crossroad;

import app.backend.document.TimeInterval;
import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CrossroadsUtils {
    @Autowired UserService userService;
    @Autowired CrossroadService crossroadService;
    @Autowired RoadService roadService;
    @Autowired CollisionService collisionService;
    @Autowired TrafficLightService trafficLightService;
    @Autowired ConnectionService connectionService;
    @Autowired CarFlowService carFlowService;
    @Autowired OptimizationService optimizationService;
    @Autowired VideoService videoService;

    public void analyseVideo(String videoId) {
        URL url;
        try {
            url = new URL("http://localhost:8081/analysis");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept", "application/json");

            JSONObject body = new JSONObject();
            body.put("id", videoId);
            body.put("extension", videoService.getVideo(videoId).getType().split("/")[1]); // hopefully Lob lazily loaded; TODO: check in the future

            System.out.println(body.toString(4));
            // XDDDD https://stackoverflow.com/questions/68089776/422-unprocessable-entity-error-when-posting-a-fastapi-docker-container-but-works
            byte[] out = body.toString(4).getBytes(StandardCharsets.UTF_8);
            OutputStream stream = connection.getOutputStream();
            stream.write(out);
            System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage()); // 200 OK
            System.out.println(new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8)); // return value
            connection.disconnect();
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    public void addOptimizationResultsToDb(String crossroadId, TimeInterval timeInterval, String results) {
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
                timeInterval.getId(),
                sequences
        );
    }

    public String parseOutput(String text, @PathVariable String crossroadId) throws Exception {
        JSONObject obj = new JSONObject(text);
        JSONArray arr = obj.getJSONArray("results");

        Map<Integer, JSONArray> map = new HashMap<>();
        for(int i=0; i<arr.length(); i++){
            map.put(i+1, arr.getJSONArray(i));
        }

        Crossroad crossroadDB = crossroadService.getCrossroadById(crossroadId);
        List<String> connectionsDB = crossroadDB.getConnectionIds();

        List<List<Integer>> roadConnections = connectionsDB
                .stream()
                .map(connectionId -> {
                    try {
                        List<Integer> innerList = new ArrayList<>();
                        for(int i=0; i<connectionService.getConnectionById(connectionId).getTrafficLightIds().size(); i++) {
                            innerList.add(trafficLightService.getTrafficLightById(connectionService.getConnectionById(connectionId).getTrafficLightIds().get(i)).getIndex());
                        }
                        return innerList;
                    } catch (Exception e) {e.printStackTrace();}
                    return new ArrayList<Integer>();
                }).toList();

        List<Integer> flows = connectionsDB
                .stream()
                .map(connectionId -> {
                    try {
                        String carFlowID = connectionService.getConnectionById(connectionId).getCarFlowIds().get(0);
                        return carFlowService.getCarFlowById(carFlowID).getCarFlow();
                    } catch (Exception e) {e.printStackTrace();}
                    return 1;
                }).toList();

        JSONObject response = new JSONObject();
        JSONArray JsonConnections = new JSONArray();
        for(int i=0; i<roadConnections.size(); i++){
            JSONObject JsonConnection = new JSONObject();

            JSONArray JsonLights = new JSONArray();
            float possibleFlow = 0;
            for(int light:roadConnections.get(i)) {
                JSONObject JsonLight = new JSONObject();
                JsonLight.put("lightId", light);
                JSONArray sequence = map.get(light);
                JsonLight.put("sequence", sequence);
                JsonLight.put("direction", "forward"); // TODO

                JsonLights.put(JsonLight);

                possibleFlow += countOccurrences(sequence, 1);
            }
            float expectedFlow = flows.get(i);
            final DecimalFormat df = new DecimalFormat("0.00");
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            JsonConnection.put("flow", df.format(possibleFlow / expectedFlow));

            JsonConnection.put("lights", JsonLights);

            JsonConnections.put(JsonConnection);
        }
        response.put("connections", JsonConnections);

        return response.toString();
    }

    public static int countOccurrences(JSONArray jsonArray, int target) throws JSONException {
        int count = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getInt(i) == target) {
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
                    .collect(Collectors.partitioningBy(collisionId -> {
                        try {
                            return collisionService.getCollisionById(collisionId).getType().equals(CollisionType.LIGHT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }));

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
            List<Integer> carFlows = connections
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
