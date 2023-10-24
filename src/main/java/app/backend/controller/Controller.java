package app.backend.controller;

import app.backend.document.CarFlow;
import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.TimeInterval;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import app.backend.document.light.TrafficLight;
import app.backend.document.road.Road;
import app.backend.document.road.RoadType;
import app.backend.service.CarFlowService;
import app.backend.service.CollisionService;
import app.backend.service.ConnectionService;
import app.backend.service.CrossroadService;
import app.backend.service.RoadService;
import app.backend.service.TimeIntervalService;
import app.backend.service.TrafficLightService;
import app.backend.service.UserService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static app.backend.document.light.TrafficLightType.ARROW_LEFT;
import static app.backend.document.light.TrafficLightType.FORWARD;
import static app.backend.document.light.TrafficLightType.LEFT;

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
    @Autowired
    TimeIntervalService timeIntervalService;

    @ResponseBody
    @GetMapping(value = "/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello world!");
    }

    private List<JSONArray> mapCollisions(List<String> collisions) {
        return collisions
                .stream()
                .map(collisionId -> {
                    Collision collision = collisionService.getCollisionById(collisionId);
                    if (collision == null) {
                        System.out.println("WARN: collision not found");
                        return new JSONArray();
                    }

                    return new JSONArray(
                            Arrays.asList(
                                    collision.getConnection1Id(),
                                    collision.getConnection2Id()
                            )
                    );
                })
                .toList();
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
            TrafficLight trafficLight;
            if (i % 3 == 0) {
                trafficLight = trafficLightService.addTrafficLight(i + 1, LEFT);
            } else if (i % 3 == 1) {
                trafficLight = trafficLightService.addTrafficLight(i + 1, FORWARD);
            } else {
                trafficLight = trafficLightService.addTrafficLight(i + 1, ARROW_LEFT);
            }
            lightsIDs.add(trafficLight.getId());
        }
        return lightsIDs;
    }

    private ArrayList<String> populateCarFlows(TimeInterval timeInterval) {
        ArrayList<String> carFlowsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfConnections; i++) {
            int carFlowValue;
            if (i % 3 == 0) {
                carFlowValue = 5;
            } else if (i % 3 == 1) {
                carFlowValue = 10;
            } else {
                carFlowValue = 12;
            }
            CarFlow carFlow = carFlowService.addCarFlow(carFlowValue, timeInterval.getId());
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
                type = RoadType.EXIT;
            } else {
                type = RoadType.ENTRANCE;
            }
            int capacity = -1;
            Road road = roadService.addRoad(i + 1, name, type, capacity, Float.valueOf(String.valueOf(i)), Float.valueOf(String.valueOf(i)));
            roadsIDs.add(road.getId());
        }
        return roadsIDs;
    }

    private String addCollision(int index, String name, int con1, int con2, ArrayList<String> lightsType, ArrayList<String> lightsIDs) {
        boolean bothCanBeOn;
        bothCanBeOn = !Objects.equals(lightsType.get(con1), "heavy") &&
                !Objects.equals(lightsType.get(con2), "heavy");
        String trafficLight1Id = lightsIDs.get(con1);
        String trafficLight2Id = lightsIDs.get(con2);
        Collision collision = collisionService.addCollision(index, name, trafficLight1Id, trafficLight2Id, bothCanBeOn);
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

        int index = 1;
        for (int light1 = 0; light1 < numberOfLights; light1++) {
            for (int light2 = light1; light2 < numberOfLights; light2++) {
                if (light1 % 3 == 0) {
                    if (light2 == (light1 + 3) % numberOfLights ||
                            light2 == (light1 + 4) % numberOfLights ||
                            light2 == (light1 + 7) % numberOfLights ||
                            light2 == (light1 + 8) % numberOfLights ||
                            light2 == (light1 + 9) % numberOfLights ||
                            light2 == (light1 + 10) % numberOfLights) {
                        collisionsIDs.add(addCollision(index, "name", light1, light2, lightsType, lightsIDs));
                    }
                }
                if (light1 % 3 == 1) {
                    if (light2 == (light1 + 2) % numberOfLights ||
                            light2 == (light1 + 3) % numberOfLights ||
                            light2 == (light1 + 4) % numberOfLights ||
                            light2 == (light1 + 5) % numberOfLights ||
                            light2 == (light1 + 8) % numberOfLights ||
                            light2 == (light1 + 9) % numberOfLights) {
                        boolean bothCanBeOn;
                        bothCanBeOn = !Objects.equals(lightsType.get(light1), "heavy") &&
                                !Objects.equals(lightsType.get(light2), "heavy") &&
                                light2 != (light1 + 3) % numberOfLights &&
                                light2 != (light1 + 9) % numberOfLights;
                        String trafficLight1Id = lightsIDs.get(light1);
                        String trafficLight2Id = lightsIDs.get(light2);
                        Collision collision = collisionService.addCollision(index, "name", trafficLight1Id, trafficLight2Id, bothCanBeOn);
                        collisionsIDs.add(collision.getId());
                    }
                }
                if (light1 % 3 == 2) {
                    if (light2 == (light1 + 4) % numberOfLights || light2 == (light1 + 8) % numberOfLights) {
                        collisionsIDs.add(addCollision(index, "name", light1, light2, lightsType, lightsIDs));
                    }
                }
                index++;
            }
        }
        return collisionsIDs;
    }

    private ArrayList<String> populateConnections(
            ArrayList<String> lightsIDs,
            ArrayList<String> carFlowsIDs,
            ArrayList<String> roadsIDs
    ) {
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
            carFlowsIDsTMP.add(carFlowsIDs.get(i * 3));
            connection = connectionService.addConnection(3 * i + 1, "name", lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1) + 4) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(carFlowsIDs.get(i * 3 + 1));
            connection = connectionService.addConnection(3 * i + 2, "name", lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());


            sourceId = roadsIDs.get((i * 3 + 2) % numberOfRoads);
            targetId = roadsIDs.get(((i * 3 + 1 + 1)) % numberOfRoads);
            lightsIDsTMP.clear();
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 1));
            lightsIDsTMP.add(lightsIDs.get(i * 3 + 2));
            carFlowsIDsTMP.clear();
            carFlowsIDsTMP.add(carFlowsIDs.get(i * 3 + 2));
            connection = connectionService.addConnection(3 * i + 3, "name", lightsIDsTMP, sourceId, targetId, carFlowsIDsTMP);
            collisionsIDs.add(connection.getId());
        }
        return collisionsIDs;
    }

    private TimeInterval populateTimeIntervals() {
        return timeIntervalService.addTimeInterval(LocalTime.ofSecondOfDay(0), LocalTime.ofSecondOfDay(1600));
    }

    private String populateAll() {
        TimeInterval timeInterval = populateTimeIntervals();
        ArrayList<String> lightsIDs = populateLights();
        ArrayList<String> carFlowsIDs = populateCarFlows(timeInterval);
        ArrayList<String> roadsIDs = populateRoads();
        ArrayList<String> collisionsIDs = populateCollisions(lightsIDs);
        ArrayList<String> connectionsIDs = populateConnections(lightsIDs, carFlowsIDs, roadsIDs);

        int randomName = new Random().nextInt(100);
        String name = "Crossroad" + randomName;
        String location = "Kijowska-KWielkiego";
        String creatorId = "NK";
        CrossroadType type = CrossroadType.PUBLIC;
        String imageId = "nosuchimage";

        Crossroad crossroad = crossroadService.addCrossroad(
                name,
                location,
                creatorId,
                type,
                roadsIDs,
                collisionsIDs,
                connectionsIDs,
                lightsIDs,
                imageId
        );

        crossroadService.getCrossroadById(crossroad.getId());

        return crossroad.getId() + ";" + timeInterval.getId();
    }
}