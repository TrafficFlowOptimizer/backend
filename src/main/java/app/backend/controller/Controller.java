package app.backend.controller;

import app.backend.document.CarFlow;
import app.backend.document.Connection;
import app.backend.document.TrafficLight;
import app.backend.document.collision.Collision;
import app.backend.document.collision.CollisionType;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import app.backend.document.road.Road;
import app.backend.document.road.RoadType;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
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

    @GetMapping(value="/test")
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
            List<JSONArray> lightsLightCollisions = lightCollisions
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
            List<JSONArray> lightsHeavyCollisions = heavyCollisions
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
            json.append("lights_heavy_collisions", lightsHeavyCollisions);
            json.append("heavy_collisions_no", numberOfHeavyCollisions);
            json.append("lights_light_collisions", lightsLightCollisions);
            json.append("light_collisions_no", numberOfLightCollisions);
            json.append("car_flow_per_min", carFlows);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    @GetMapping(value="/sample-data")
    public String populateDb() {
        this.populate_all();

        return "here";
    }

    final int numberOfLights = 12;
    final int numberOfRoads = 12;
    final int numberOfConnections = 12;

    private ArrayList<String> populate_lights() {
        ArrayList<String> lightsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfLights; i++) {
            TrafficLight trafficLight = trafficLightService.addTrafficLight();
            lightsIDs.add(trafficLight.getId());
        }
        return lightsIDs;
    }

    private ArrayList<String> populate_car_flows() {
        ArrayList<String> car_flowsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfConnections; i++) {
            Random ran = new Random();
            int car_flow;
            if (i % 3 == 0) {
                car_flow = ran.nextInt(6) + 5;
            } else if (i % 3 == 1) {
                car_flow = ran.nextInt(6) + 10;
            } else {
                car_flow = ran.nextInt(6) + 15;
            }
            LocalTime start = LocalTime.ofSecondOfDay(0);
            LocalTime end = LocalTime.ofSecondOfDay(1600);
            CarFlow carFlow = carFlowService.addCarFlow(car_flow, start, end);
            car_flowsIDs.add(carFlow.getId());
        }
        return car_flowsIDs;
    }

    private ArrayList<String> populate_roads() {
        ArrayList<String> roadsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfRoads; i++) {
            String name = "Street_" + i;
            RoadType type;
            if (i % 3 == 0) {
                type = RoadType.TARGET;
            } else {
                type = RoadType.SOURCE;
            }
            int capacity = -1;
            Road road = roadService.addRoad(name, type, capacity);
            roadsIDs.add(road.getId());
        }
        return roadsIDs;
    }

    private ArrayList<String> populate_collisions(ArrayList<String> lightsIDs) {
        ArrayList<String> collisionsIDs = new ArrayList<>();

        ArrayList<String> lights_type = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lights_type.add("heavy");
            lights_type.add("light");
            lights_type.add("light");
        }


        for (int light1 = 0; light1 < numberOfLights; light1++) {
            for (int light2 = light1; light2 < numberOfLights; light2++) {
                if (light1 % 3 == 0) {
                    if (light2 == (light1 + 3) % numberOfLights ||
                            light2 == (light1 + 4) % numberOfLights ||
                            light2 == (light1 + 7) % numberOfLights ||
                            light2 == (light1 + 8) % numberOfLights ||
                            light2 == (light1 + 9) % numberOfLights ||
                            light2 == (light1 + 10) % numberOfLights) {
                        CollisionType type;
                        if (Objects.equals(lights_type.get(light1), "heavy") ||
                                Objects.equals(lights_type.get(light2), "heavy")) {

                            type = CollisionType.HEAVY;
                        } else {
                            type = CollisionType.LIGHT;
                        }
                        String trafficLight1Id = lightsIDs.get(light1);
                        String trafficLight2Id = lightsIDs.get(light2);
                        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
                        collisionsIDs.add(collision.getId());
                    }
                }
                if (light1 % 3 == 1) {
                    if (light2 == (light1 + 2) % numberOfLights ||
                            light2 == (light1 + 3) % numberOfLights ||
                            light2 == (light1 + 4) % numberOfLights ||
                            light2 == (light1 + 5) % numberOfLights ||
                            light2 == (light1 + 8) % numberOfLights ||
                            light2 == (light1 + 9) % numberOfLights) {
                        CollisionType type;
                        if (Objects.equals(lights_type.get(light1), "heavy") ||
                                Objects.equals(lights_type.get(light2), "heavy") ||
                                light2 == (light1 + 3) % numberOfLights ||
                                light2 == (light1 + 9) % numberOfLights) {
                            type = CollisionType.HEAVY;
                        } else {
                            type = CollisionType.LIGHT;
                        }
                        String trafficLight1Id = lightsIDs.get(light1);
                        String trafficLight2Id = lightsIDs.get(light2);
                        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
                        collisionsIDs.add(collision.getId());
                    }
                }
                if (light1 % 3 == 2) {
                    if (light2 == (light1 + 4) % numberOfLights || light2 == (light1 + 8) % numberOfLights) {
                        CollisionType type;
                        if (Objects.equals(lights_type.get(light1), "heavy") ||
                                Objects.equals(lights_type.get(light2), "heavy")) {
                            type = CollisionType.HEAVY;
                        } else {
                            type = CollisionType.LIGHT;
                        }
                        String trafficLight1Id = lightsIDs.get(light1);
                        String trafficLight2Id = lightsIDs.get(light2);
                        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
                        collisionsIDs.add(collision.getId());
                    }
                }
            }
        }

        return collisionsIDs;
    }

    private ArrayList<String> populate_connections(ArrayList<String> lightsIDs,
                                                   ArrayList<String> car_flowsIDs,
                                                   ArrayList<String> roadsIDs) {
        ArrayList<String> collisionsIDs = new ArrayList<>();


        for (int i = 0; i < 4; i++) {
            ArrayList<String> lightsIDsTMP = new ArrayList<>();
            String sourceId;
            String targetId;
            ArrayList<String> carFlowsIDsTMP = new ArrayList<>();
            Connection connection;


            sourceId = roadsIDs.get((i * 3 + 1) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1) + 8) % numberOfRoads);
            lightsIDsTMP.add(lightsIDs.get(i * 3));
            carFlowsIDsTMP.add(car_flowsIDs.get(i));
            connection = connectionService.addConnection(lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1) + 4) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(car_flowsIDs.get(i + 1));
            connection = connectionService.addConnection(lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1 + 1)) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 2));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(car_flowsIDs.get(i + 2));
            connection = connectionService.addConnection(lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());
        }
        return collisionsIDs;
    }

    private void populate_all() {
        ArrayList<String> lightsIDs = populate_lights();
        ArrayList<String> car_flowsIDs = populate_car_flows();
        ArrayList<String> roadsIDs = populate_roads();
        ArrayList<String> collisionsIDs = populate_collisions(lightsIDs);
        ArrayList<String> connectionsIDs = populate_connections(lightsIDs, car_flowsIDs, roadsIDs);


        String name = "Crossroad";
        String location = "Kijowska-KWielkiego";
        String creatorId = "NK";
        CrossroadType type = CrossroadType.PUBLIC;

        Crossroad crossroad = crossroadService.addCrossroad(name, location, creatorId, type, roadsIDs,
                collisionsIDs, connectionsIDs, lightsIDs);

        Crossroad found = null;
        try {
            found = crossroadService.getCrossroadById(crossroad.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}