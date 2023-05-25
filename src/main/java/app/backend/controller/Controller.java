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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class Controller {

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

    @ResponseBody
    @GetMapping(value = "/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello world!");
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

    @GetMapping(value = "/test/{crossroadId}")
    public String parseJSON(@PathVariable String crossroadId) {
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

        return jsonBase.toString();
    }

    @GetMapping(value = "/sample-data")
    public String populateDb() {
        return this.populateAll();
    }

    final int numberOfLights = 12;
    final int numberOfRoads = 12;
    final int numberOfConnections = 12;

    private ArrayList<String> populateLights() {
        ArrayList<String> lightsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfLights; i++) {
            TrafficLight trafficLight = trafficLightService.addTrafficLight(i);
            lightsIDs.add(trafficLight.getId());
        }
        return lightsIDs;
    }

    private ArrayList<String> populateCarFlows() {
        ArrayList<String> carFlowsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfConnections; i++) {
            Random ran = new Random();
            int carFlowValue;
            if (i % 3 == 0) {
                carFlowValue = ran.nextInt(6) + 5;
            } else if (i % 3 == 1) {
                carFlowValue = ran.nextInt(6) + 10;
            } else {
                carFlowValue = ran.nextInt(6) + 15;
            }
            LocalTime start = LocalTime.ofSecondOfDay(0);
            LocalTime end = LocalTime.ofSecondOfDay(1600);
            CarFlow carFlow = carFlowService.addCarFlow(carFlowValue, start, end);
            carFlowsIDs.add(carFlow.getId());
        }
        return carFlowsIDs;
    }

    private ArrayList<String> populateRoads() {
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
            Road road = roadService.addRoad(i, name, type, capacity);
            roadsIDs.add(road.getId());
        }
        return roadsIDs;
    }

    private String addCollision(int index, int light1, int light2, ArrayList<String> lightsType, ArrayList<String> lightsIDs){
        CollisionType type;
        if (Objects.equals(lightsType.get(light1), "heavy") ||
                Objects.equals(lightsType.get(light2), "heavy")) {

            type = CollisionType.HEAVY;
        } else {
            type = CollisionType.LIGHT;
        }
        String trafficLight1Id = lightsIDs.get(light1);
        String trafficLight2Id = lightsIDs.get(light2);
        Collision collision = collisionService.addCollision(index, trafficLight1Id, trafficLight2Id, type);
        return collision.getId();
    }

    private ArrayList<String> populateCollisions(ArrayList<String> lightsIDs) {
        ArrayList<String> collisionsIDs = new ArrayList<>();

        ArrayList<String> lightsType = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lightsType.add("heavy");
            lightsType.add("light");
            lightsType.add("light");
        }

        int index = 0;
        for (int light1 = 0; light1 < numberOfLights; light1++) {
            for (int light2 = light1; light2 < numberOfLights; light2++) {
                if (light1 % 3 == 0) {
                    if (light2 == (light1 + 3) % numberOfLights ||
                            light2 == (light1 + 4) % numberOfLights ||
                            light2 == (light1 + 7) % numberOfLights ||
                            light2 == (light1 + 8) % numberOfLights ||
                            light2 == (light1 + 9) % numberOfLights ||
                            light2 == (light1 + 10) % numberOfLights) {
                        collisionsIDs.add(addCollision(index, light1, light2, lightsType, lightsIDs));
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
                        if (Objects.equals(lightsType.get(light1), "heavy") ||
                                Objects.equals(lightsType.get(light2), "heavy") ||
                                light2 == (light1 + 3) % numberOfLights ||
                                light2 == (light1 + 9) % numberOfLights) {
                            type = CollisionType.HEAVY;
                        } else {
                            type = CollisionType.LIGHT;
                        }
                        String trafficLight1Id = lightsIDs.get(light1);
                        String trafficLight2Id = lightsIDs.get(light2);
                        Collision collision = collisionService.addCollision(index, trafficLight1Id, trafficLight2Id, type);
                        collisionsIDs.add(collision.getId());
                    }
                }
                if (light1 % 3 == 2) {
                    if (light2 == (light1 + 4) % numberOfLights || light2 == (light1 + 8) % numberOfLights) {
                        collisionsIDs.add(addCollision(index, light1, light2, lightsType, lightsIDs));
                    }
                }
                index++;
            }
        }
        return collisionsIDs;
    }

    private ArrayList<String> populateConnections(ArrayList<String> lightsIDs,
                                                  ArrayList<String> carFlowsIDs,
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
            carFlowsIDsTMP.add(carFlowsIDs.get(i));
            connection = connectionService.addConnection(3*i, lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1) + 4) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(carFlowsIDs.get(i + 1));
            connection = connectionService.addConnection(3*i + 1, lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1 + 1)) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 2));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(carFlowsIDs.get(i + 2));
            connection = connectionService.addConnection(3*i + 2, lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());
        }
        return collisionsIDs;
    }

    private String populateAll() {
        ArrayList<String> lightsIDs = populateLights();
        ArrayList<String> carFlowsIDs = populateCarFlows();
        ArrayList<String> roadsIDs = populateRoads();
        ArrayList<String> collisionsIDs = populateCollisions(lightsIDs);
        ArrayList<String> connectionsIDs = populateConnections(lightsIDs, carFlowsIDs, roadsIDs);

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

        return crossroad.getId();
    }
}