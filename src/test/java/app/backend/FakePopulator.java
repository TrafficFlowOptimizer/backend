package app.backend;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalTime;
import java.util.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FakePopulator {

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

    final int numberOfLights = 12;
    final int numberOfRoads = 12;
    final int numberOfConnections = 12;


    ArrayList<String> populate_lights() {
        ArrayList<String> lightsIDs = new ArrayList<>();
        for (int i = 0; i < numberOfLights; i++) {
            TrafficLight trafficLight = trafficLightService.addTrafficLight();
            lightsIDs.add(trafficLight.getId());
        }
        return lightsIDs;
    }

    ArrayList<String> populate_car_flows() {
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

    ArrayList<String> populate_roads() {
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

    ArrayList<String> populate_collisions(ArrayList<String> lightsIDs) {
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

    ArrayList<String> populate_connections(ArrayList<String> lightsIDs,
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

    @Test
    void populate_all() {
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
