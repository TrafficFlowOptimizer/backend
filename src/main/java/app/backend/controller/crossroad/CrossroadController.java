package app.backend.controller.crossroad;

import app.backend.document.crossroad.Crossroad;
import app.backend.request.crossroad.CrossroadDescriptionRequest;
import app.backend.response.crossroad.CrossroadDescriptionResponse;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(value = "/crossroad")
public class CrossroadController {

    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final ConnectionService connectionService;
    private final TrafficLightService trafficLightService;
    private final UserService userService;
    private final OptimizationService optimizationService;
    private final CrossroadsUtils crossroadsUtils;

    @Autowired
    public CrossroadController(
            CrossroadService crossroadService,
            RoadService roadService,
            CollisionService collisionService,
            ConnectionService connectionService,
            TrafficLightService trafficLightService,
            UserService userService,
            OptimizationService optimizationService,
            CrossroadsUtils crossroadsUtils
    ) {
        this.crossroadService = crossroadService;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.connectionService = connectionService;
        this.trafficLightService = trafficLightService;
        this.userService = userService;
        this.optimizationService = optimizationService;
        this.crossroadsUtils = crossroadsUtils;
    }

    @GetMapping
    public ResponseEntity<List<Crossroad>> getUserCrossroads(@RequestParam(required = false) String userId) {
        // for now if userId passed then returns PRIVATE for user and PUBLIC, else PUBLIC. In the future using session it will return PRIVATE for user and PUBLIC
        // maybe in the future option to get only privates or publics??
        if(userId != null) {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getCrossroadsByCreatorIdOrPublic(userId));
        }
        else {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getPublicCrossroads());
        }
    }

    @GetMapping(value="/{crossroadId}")
    public ResponseEntity<CrossroadDescriptionResponse> getCrossroad(@PathVariable String crossroadId) {
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        if (crossroad == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        CrossroadDescriptionResponse crossroadDescriptionResponse = new CrossroadDescriptionResponse(
                crossroad,
                crossroad.getRoadIds()
                        .stream()
                        .map(roadService::getRoadById)
                        .collect(Collectors.toList()),
                crossroad.getCollisionIds()
                        .stream()
                        .map(collisionService::getCollisionById)
                        .collect(Collectors.toList()),
                crossroad.getConnectionIds()
                        .stream()
                        .map(connectionService::getConnectionById)
                        .collect(Collectors.toList()),
                crossroad.getTrafficLightIds()
                        .stream()
                        .map(trafficLightService::getTrafficLightById)
                        .collect(Collectors.toList())
        );

        return ResponseEntity
                .ok()
                .body(crossroadDescriptionResponse);
    }

    @PostMapping() // TODO: try catch nosuchelement
    public ResponseEntity<Boolean> addCrossroad(@RequestBody CrossroadDescriptionRequest crossroadDescriptionRequest) {

        List<String> roadIds = crossroadDescriptionRequest
                .getRoads()
                .stream()
                .map( roadRequest -> roadService.addRoad(
                                roadRequest.getIndex(),
                                roadRequest.getName(),
                                roadRequest.getType(),
                                roadRequest.getCapacity()
                        ).getId()
                )
                .collect(Collectors.toList());

        List<String> trafficLightsIds = crossroadDescriptionRequest
                .getTrafficLights()
                .stream()
                .map( trafficLightRequest -> trafficLightService.addTrafficLight(
                        trafficLightRequest.getIndex(),
                        trafficLightRequest.getType()
                ).getId())
                .collect(Collectors.toList());

        List<String> connectionIds = crossroadDescriptionRequest
                .getConnections()
                .stream()
                .map( connectionRequest -> connectionService.addConnection(
                        connectionRequest.getIndex(),
                        connectionRequest.getName(),
                        trafficLightsIds
                                .stream()
                                .filter( trafficLightId -> connectionRequest.getTrafficLightIds() != null && connectionRequest.getTrafficLightIds().contains(trafficLightService.getTrafficLightById(trafficLightId).getIndex()))
                                .collect(Collectors.toList()),
                        roadIds.stream()
                                .filter( roadId -> roadService.getRoadById(roadId).getIndex() == connectionRequest.getSourceId())
                                .findAny()
                                .orElseThrow(),
                        roadIds.stream()
                                .filter( roadId -> roadService.getRoadById(roadId).getIndex() == connectionRequest.getTargetId())
                                .findAny()
                                .orElseThrow(),
                        Collections.emptyList()
                ).getId())
                .collect(Collectors.toList());

        List<String> collisionIds = crossroadDescriptionRequest
                .getCollisions()
                .stream()
                .map( collisionRequest -> collisionService.addCollision(
                        collisionRequest.getIndex(),
                        collisionRequest.getName(),
                        connectionIds
                                .stream()
                                .filter( connectionId -> connectionService.getConnectionById(connectionId).getIndex() == collisionRequest.getConnection1Id())
                                .findAny()
                                .orElseThrow(),
                        connectionIds
                                .stream()
                                .filter( connectionId -> connectionService.getConnectionById(connectionId).getIndex() == collisionRequest.getConnection2Id())
                                .findAny()
                                .orElseThrow(),
                        collisionRequest.getBothCanBeOn()
                ).getId())
                .collect(Collectors.toList());

        crossroadService.addCrossroad(
                crossroadDescriptionRequest.getCrossroad().getName(),
                crossroadDescriptionRequest.getCrossroad().getLocation(),
                crossroadDescriptionRequest.getCrossroad().getCreatorId(),
                crossroadDescriptionRequest.getCrossroad().getType(),
                roadIds,
                collisionIds,
                connectionIds,
                trafficLightsIds
        );

        return ResponseEntity
                .ok()
                .body(true);
    }

    @GetMapping(value="/{crossroadId}/optimization/{videoId}/{time}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOptimization( // TODO: change to JSONObject
            @PathVariable String crossroadId,
            @PathVariable String videoId,
            @PathVariable int time
    ) {
        int serverPort = 9091; // TODO: get from variable from environment
        String result = "{}";
        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = crossroadsUtils.parseJSON(crossroadId, time);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(jsonData);

            InputStream optimizerResponse = socket.getInputStream();
            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";

            String timeIntervalId = crossroadsUtils.getTimeIntervalId(videoId);
            if (timeIntervalId == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }
            crossroadsUtils.addOptimizationResultsToDb(crossroadId, timeIntervalId, result);
            throw new RuntimeException(); //TODO: for now
//            result = crossroadsUtils.parseOutput(result, crossroadId);

        } catch (Exception e) {
            try {
                sleep(time*1000L);
                result = Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {}
        }
        System.out.println(result);

        return ResponseEntity
                .ok()
                .body(result);
    }

    private List<List<Integer>> convertJSONArrayToArray(String jsonArray){
        JSONObject obj = new JSONObject(jsonArray);
        JSONArray arr = obj.getJSONArray("results");
        List<List<Integer>> ar = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            List<Integer> row = new ArrayList<>();
            for(int j=0;j<arr.getJSONArray(0).length();j++){
                row.add((int) arr.getJSONArray(i).get(j));
            }
            ar.add(row);
        }
        return ar;
    }

    @GetMapping(value="/{crossroadId}/optimization/{time}",  produces = MediaType.APPLICATION_JSON_VALUE) // TODO
    public String getOptimizationWithoutVideo(@PathVariable String crossroadId, @PathVariable int time) {
        int serverPort = 9091;
        String result = "{results: \"ERROR\"}";
        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = crossroadsUtils.parseJSON(crossroadId, time);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(jsonData);

            InputStream optimizerResponse = socket.getInputStream();
            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";
            List<List<Integer>> resultArray = convertJSONArrayToArray(result);
            //TODO: timeIntervalID powinno być jako argument

            optimizationService.addOptimization(crossroadId, "0", resultArray);

            result = getOptimizationResults(crossroadId, "0");

        } catch (Exception e) {
            try {
                sleep(time*1000L);
                result = Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {}
        }
        System.out.println(result);

        return result;
    }

    @GetMapping(value="/{crossroadId}/optimization_results",  produces = MediaType.APPLICATION_JSON_VALUE) // TODO
    public String getOptimizationResults( @PathVariable String crossroadId, String timeIntervalID){
        try {
            List<List<Integer>> newestResult = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            List<List<Integer>> secondNewestResult = optimizationService.getSecondNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            return crossroadsUtils.parseOutput(newestResult, secondNewestResult, crossroadId);
        } catch (Exception e) {
            try {
                return Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {
                throw new RuntimeException("Error with reading template output.");
            }
        }
    }
}
