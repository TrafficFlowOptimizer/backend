package app.backend.controller;

import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class Controller {

    @Autowired UserService userService;
    @Autowired CrossroadService crossroadService;
    @Autowired RoadService roadService;
    @Autowired CollisionService collisionService;
    @Autowired TrafficLightService trafficLightService;
    @Autowired ConnectionService connectionService;
    @Autowired CarFlowService carFlowService;

    @ResponseBody
    @GetMapping(value="/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello world!");
    }

    @GetMapping(value="/test/")
    public String parseJSON() {

        // random time for now

        JSONObject json = new JSONObject();
        String crossroadId = "id";

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

            List<String> roads = crossroad.getRoadIds();
            int numberOfRoads = roads.size();

            List<String> collisions = crossroad.getRoadIds();
            Map<Boolean, List<String>> collisionsDivided = collisions
                    .stream()
                    .collect(Collectors.partitioningBy(collisionId -> {
                        try {
                            return collisionService.getCollisionById(collisionId).getType().equals(CollisionType.LIGHT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false; // ?
                    }));

            List<String> lightCollisions = collisionsDivided.get(true);
            List<String> heavyCollisions = collisionsDivided.get(false);
            int numberOfLightCollisions = lightCollisions.size();
            int numberOfHeavyCollisions = heavyCollisions.size();
            List<JSONArray> lightsLightConflicts = lightCollisions
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
            List<JSONArray> lightsHeavyConflicts = heavyCollisions
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

            List<String> connections = crossroad.getConnectionIds();
            int numberOfConnections = connections.size();

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

            // TODO: finish
            List<JSONArray> roadConnections = connections
                    .stream()
                    .map(connectionId -> {
                        try {
                            return new JSONArray(
                                Arrays.asList(
                                        connectionService.getConnectionById(connectionId).getSourceId(),
                                        connectionService.getConnectionById(connectionId).getTargetId()
                                )
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new JSONArray();
                    }).toList();

            List<String> lights = crossroad.getTrafficLightIds();
            int numberOfLights = lights.size();

            json.append("time_units_in_minute", 60); // fixed for now
            json.append("number_of_time_units", 60); // fixed for now
            json.append("number_of_lights", numberOfLights);
            json.append("number_of_roads", numberOfRoads);
            json.append("number_of_connections", numberOfConnections);
            json.append("lights_type", new JSONArray()); // optional
            json.append("roads_connections", roadConnections);
            json.append("lights", lights);
            json.append("lights_heavy_conflicts", lightsHeavyConflicts);
            json.append("heavy_conflicts_no", numberOfHeavyCollisions);
            json.append("lights_light_conflicts", lightsLightConflicts);
            json.append("light_conflicts_no", numberOfLightCollisions);
            json.append("car_flow_per_min", carFlows);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

}
