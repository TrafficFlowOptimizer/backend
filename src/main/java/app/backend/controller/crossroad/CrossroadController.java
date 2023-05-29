package app.backend.controller.crossroad;

import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CrossroadController {

    @Autowired
    UserService userService;
    @Autowired
    CrossroadService crossroadService;
    @Autowired
    RoadService roadService;
    @Autowired
    CollisionService collisionService;
    @Autowired
    TrafficLightService trafficLightService;
    @Autowired
    ConnectionService connectionService;
    @Autowired
    CarFlowService carFlowService;

    @GetMapping(value="/crossroad")
    public List<Crossroad> getUserCrossroads(@RequestParam(required = false) String userId) {
        // for now if userId passed then returns PRIVATE for user and PUBLIC, else PUBLIC. In the future using session it will return PRIVATE for user and PUBLIC
        // maybe in the future option to get only privates or publics??
        if(userId != null) {
            return crossroadService.getCrossroadsByCreatorIdOrPublic(userId);
        }
        else {
            return crossroadService.getCrossroadsPublic();
        }
    }

    @GetMapping(value="/crossroad/{crossroadId}")
    public Crossroad getCrossroad(@PathVariable String crossroadId) {
        Crossroad crossroad = null;
        try {
            crossroad = crossroadService.getCrossroadById(crossroadId);
        } catch (Exception e) {e.printStackTrace();}
        return crossroad;
    }

    @PostMapping(value="/crossroad")
    public String addCrossroad(@RequestBody Crossroad crossroad) {
        crossroadService.addCrossroad(
                crossroad.getName(), crossroad.getLocation(), crossroad.getCreatorId(), crossroad.getType(),
                crossroad.getRoadIds(), crossroad.getCollisionIds(), crossroad.getConnectionIds(), crossroad.getTrafficLightIds()
        );
        return "ok";
    }

    @PutMapping(value="/crossroad")
    public String updateCrossroad(@RequestBody Crossroad crossroad) {
        try {
            crossroadService.updateCrossroad( crossroad.getId(),
                    crossroad.getName(), crossroad.getLocation(), crossroad.getCreatorId(), crossroad.getType(),
                    crossroad.getRoadIds(), crossroad.getCollisionIds(), crossroad.getConnectionIds(), crossroad.getTrafficLightIds()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @GetMapping(value="/crossroad/{crossroadId}/optimization/{time}",  produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOptimization(@PathVariable String crossroadId, @PathVariable int time) {
        int serverPort = 9091;
        String result = "{}";
        try {
            result = new String(Files.readAllBytes(Paths.get("templateOutput.json")));
        }
        catch (Exception ignored){}
//        try (Socket socket = new Socket("localhost", serverPort)) {
//            JSONObject jsonData = this.parseJSON(crossroadId, time);
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//            out.println(jsonData);
//
//            InputStream optimizerResponse =  socket.getInputStream();
//            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
//            result = s.hasNext() ? s.next() : "";
//            System.out.println(result);
//        } catch (IOException ignored) {}

        return result;
    }

    private JSONObject parseJSON(@PathVariable String crossroadId, @PathVariable int time) {
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

            List<Integer> lights_IDs = new ArrayList<>();
            for (int i = 1; i <= numberOfLights; i++){
                lights_IDs.add(i);
            }
            json.put("lights_IDs", lights_IDs);

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

    private List<JSONArray> mapCollisions(List<String> collisions) {
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
