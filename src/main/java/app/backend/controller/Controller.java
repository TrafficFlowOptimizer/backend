//package app.backend.controller;
//
//import app.backend.document.collision.Collision;
//import app.backend.document.collision.CollisionType;
//import app.backend.document.crossroad.Crossroad;
//import app.backend.service.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//public class Controller {
//
//    @Autowired UserService userService;
//    @Autowired CrossroadService crossroadService;
//    @Autowired RoadService roadService;
//    @Autowired CollisionService collisionService;
//    @Autowired TrafficLightService trafficLightService;
//    @Autowired ConnectionService connectionService;
//    @Autowired CarFlowService carFlowService;
//
//    @ResponseBody
//    @GetMapping(value="/")
//    public ResponseEntity<String> home() {
//        return ResponseEntity.ok("Hello world!");
//    }
//
//    @GetMapping(value="/test/")
//    public String parseJSON() {
//
////      example object:
////      object		{13}
////        time_units_in_minute	:	60
////        number_of_time_units	:	60
////        number_of_lights	:	12
////        number_of_roads	:	12
////        number_of_connections	:	12
////        lights_type		[12]
////        roads_connections		[12]
////        lights		[12]
////        lights_heavy_conflicts		[24]
////        heavy_conflicts_no	:	24
////        lights_light_conflicts		[4]
////        light_conflicts_no	:	4
////        car_flow_per_min		[12]
//
//        String crossroadId = "id";
//        try {
//            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
//
//            List<String> roads = crossroad.getRoadIds();
//            int numberOfRoads = roads.size();
////            for(String roadId : roads) {}
//
//            List<String> collisions = crossroad.getRoadIds();
//            int numberOfCollisions = collisions.size();
//            Map<Boolean, List<String>> collisionsDivided = collisions
//                    .stream()
//                    .collect(Collectors.partitioningBy(collisionId -> {
//                        try {
//                            return collisionService.getCollisionById(collisionId).getType().equals(CollisionType.LIGHT);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return false; // ?
//                    }));
//
//            List<String> lightCollisions = collisionsDivided.get(true);
//            List<String> heavyCollisions = collisionsDivided.get(false);
//            int numberOfLightCollisions = lightCollisions.size();
//            int numberOfHeavyCollisions = heavyCollisions.size();
//            List<JSONArray> lightsLightConflicts = lightCollisions
//                    .stream()
//                    .map(collisionId -> {
//                        try {
//                            return new JSONArray(
//                                    Arrays.asList(
//                                            collisionService.getCollisionById(collisionId).getTrafficLight1Id(),
//                                            collisionService.getCollisionById(collisionId).getTrafficLight2Id()
//                                    )
//                            );
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return new JSONArray();
//                    }).toList();
//            List<JSONArray> lightsHeavyConflicts = heavyCollisions
//                    .stream()
//                    .map(collisionId -> {
//                        try {
//                            return new JSONArray(
//                                    Arrays.asList(
//                                            collisionService.getCollisionById(collisionId).getTrafficLight1Id(),
//                                            collisionService.getCollisionById(collisionId).getTrafficLight2Id()
//                                    )
//                            );
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        return new JSONArray();
//                    }).toList();
//
//            // TODO: get connections
//            // TODO: lights should come from connections !!!
//
//            Set<String> lights = new HashSet<>();
//            for(String collisionId : collisions) {
//                Collision collision = collisionService.getCollisionById(collisionId);
//                lights.add(collision.getTrafficLight1Id());
//                lights.add(collision.getTrafficLight2Id());
//            }
//            int numberOfLights = lights.size();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        JSONObject json = new JSONObject();
//
//        json.append("time_units_in_minute", 60); // fixed for now
//        json.append("number_of_time_units", 60); // fixed for now
//        json.append("number_of_lights", numberOfLights); // DONE
//        json.append("number_of_roads", numberOfRoads); // DONE
//        json.append("number_of_connections", -1); // TODO
//        json.append("lights_type", new JSONArray()); // optional
//        json.append("roads_connections", -1); // TODO
//        json.append("lights", -1); // TODO
//        json.append("lights_heavy_conflicts", lightsHeavyConflicts); // TODO
//        json.append("heavy_conflicts_no", numberOfLightCollisions); // DONE
//        json.append("lights_light_conflicts", lightsLightConflicts); // TODO
//        json.append("light_conflicts_no", numberOfLightCollisions); // DONE
//        json.append("car_flow_per_min", -1); // TODO
//
//        return json.toString();
//    }
//
//}
