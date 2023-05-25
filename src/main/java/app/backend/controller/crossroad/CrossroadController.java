package app.backend.controller.crossroad;

import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @GetMapping(value="/crossroad/{crossroadId}/optimization")
    public String getOptimization(@PathVariable String crossroadId) {
        int serverPort = 8000;

        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = this.parseJSON(crossroadId);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            out.write(jsonData.toString());
        } catch (IOException ignored) {}

        return "optimization";
    }

    private JSONObject parseJSON(@PathVariable String crossroadId) {
        JSONObject jsonBase = new JSONObject();
        jsonBase.append("time", 10);

        JSONObject json = new JSONObject();

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

//  -----------------------------  roads  -----------------------------
            List<String> roads = crossroad.getRoadIds();
            json.append("number_of_roads", roads.size());

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

            json.append("lights_light_collisions", lightsLightCollisions);
            json.append("light_collisions_no", lightsLightCollisions.size());


            List<String> heavyCollisions = collisionsDivided.get(false);
            List<JSONArray> lightsHeavyCollisions = mapCollisions(heavyCollisions);

            json.append("lights_heavy_collisions", lightsHeavyCollisions);
            json.append("heavy_collisions_no", lightsHeavyCollisions.size());

//  -----------------------------  connections  -----------------------------
            List<String> connections = crossroad.getConnectionIds();
            List<JSONArray> roadConnections = connections
                    .stream()
                    .map(connectionId -> {
                        try {
                            return new JSONArray(
                                    Arrays.asList(
                                            connectionService.getConnectionById(connectionId).getSourceId(),
                                            connectionService.getConnectionById(connectionId).getTargetId(),
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 0
                                                    ? connectionService.getConnectionById(connectionId).getTrafficLightIds().get(0) :
                                                    -1,
                                            connectionService.getConnectionById(connectionId).getTrafficLightIds().size() > 1
                                                    ? connectionService.getConnectionById(connectionId).getTrafficLightIds().get(1) :
                                                    -1
                                    )
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new JSONArray();
                    }).toList();

            json.append("roads_connections", roadConnections);
            json.append("number_of_connections", roadConnections.size());

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

            json.append("car_flow_per_min", carFlows);

//  -----------------------------  lights  -----------------------------
            List<String> lights = crossroad.getTrafficLightIds();
            int numberOfLights = lights.size();

            json.append("number_of_lights", numberOfLights);

//  -----------------------------  fixed values  -----------------------------
            json.append("time_units_in_minute", 60); // fixed for now
            json.append("number_of_time_units", 60); // fixed for now

            json.append("lights_type", new JSONArray()); // optional
            json.append("lights", new JSONArray()); // optional

        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonBase.append("configuration", json);

        return jsonBase;
    }

    private List<JSONArray> mapCollisions(List<String> collisions) {
        return collisions
                .stream()
                .map(collisionId -> {
                    try {
                        return new JSONArray(
                                Arrays.asList(
                                        collisionService.getCollisionById(collisionId).getTrafficLight1Id(),
                                        collisionService.getCollisionById(collisionId).getTrafficLight2Id()
                                )
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new JSONArray();
                }).toList();
    }
}
