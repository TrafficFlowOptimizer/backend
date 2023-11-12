package app.backend.controller.crossroad;

import app.backend.authentication.JwtUtil;
import app.backend.document.crossroad.Crossroad;
import app.backend.request.crossroad.CrossroadDescriptionRequest;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.response.crossroad.CrossroadDescriptionResponse;
import app.backend.service.*;
import app.backend.service.CollisionService;
import app.backend.service.ConnectionService;
import app.backend.service.CrossroadService;
import app.backend.service.ImageService;
import app.backend.service.OptimizationService;
import app.backend.service.RoadService;
import app.backend.service.TrafficLightService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/crossroad")
public class CrossroadController {

    @Value("${optimizer.host}")
    private String OPTIMIZER_HOST;
    @Value("${optimizer.port}")
    private int OPTIMIZER_PORT;
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final ConnectionService connectionService;
    private final TrafficLightService trafficLightService;
    private final OptimizationService optimizationService;
    private final ImageService imageService;
    private final CrossroadsUtils crossroadsUtils;
    private final JwtUtil jwtUtil;
    private final ObjectMapper jsonMapper;

    @Autowired
    public CrossroadController(
            CrossroadService crossroadService,
            RoadService roadService,
            CollisionService collisionService,
            ConnectionService connectionService,
            TrafficLightService trafficLightService,
            OptimizationService optimizationService,
            ImageService imageService,
            CrossroadsUtils crossroadsUtils,
            JwtUtil jwtUtil,
            ObjectMapper jsonMapper
    ) {
        this.crossroadService = crossroadService;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.connectionService = connectionService;
        this.trafficLightService = trafficLightService;
        this.optimizationService = optimizationService;
        this.imageService = imageService;
        this.crossroadsUtils = crossroadsUtils;
        this.jwtUtil = jwtUtil;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping
    public ResponseEntity<List<Crossroad>> getUserCrossroads(
            @RequestParam(required = false) Boolean getPrivate,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken
    ) {
        String userId = jwtUtil.getId(
                jwtUtil.parseJwtClaims(
                        jwtToken.split(" ")[1]
                )
        );

        if (getPrivate != null && getPrivate) {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getCrossroadsByCreatorIdOrPublic(userId));
        } else {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getPublicCrossroads());
        }
    }

    @GetMapping(value = "/{crossroadId}")
    public ResponseEntity<CrossroadDescriptionResponse> getCrossroad(@PathVariable String crossroadId) {
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        byte[] image = imageService.getImage(crossroad.getImageId());
        if (crossroad == null || image == null) {
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
                        .collect(Collectors.toList()),
                new String(image, StandardCharsets.UTF_8)
        );

        return ResponseEntity
                .ok()
                .body(crossroadDescriptionResponse);
    }

    @PostMapping() // TODO: delete objects when error occurs while adding
    public ResponseEntity<Boolean> addCrossroad(
            @RequestParam("description") String crossroadDescriptionRequest,
            @RequestParam("image") String image,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken
    ) {
        String creatorId = jwtUtil.getId(
                jwtUtil.parseJwtClaims(
                        jwtToken.split(" ")[1]
                )
        );

        CrossroadDescriptionRequest crossroadDescription;
        try {
            crossroadDescription = jsonMapper.readValue(crossroadDescriptionRequest, CrossroadDescriptionRequest.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .build();
        }

        List<String> roadIds = crossroadDescription
                .getRoads()
                .stream()
                .map(roadRequest -> roadService.addRoad(
                                roadRequest.getIndex(),
                                roadRequest.getName(),
                                roadRequest.getType(),
                                roadRequest.getCapacity(),
                                roadRequest.getxCord(),
                                roadRequest.getyCord()
                        ).getId()
                )
                .collect(Collectors.toList());

        List<String> trafficLightsIds = crossroadDescription
                .getTrafficLights()
                .stream()
                .map(trafficLightRequest -> trafficLightService.addTrafficLight(
                        trafficLightRequest.getIndex(),
                        trafficLightRequest.getType()
                ).getId())
                .collect(Collectors.toList());

        List<String> connectionIds = crossroadDescription
                .getConnections()
                .stream()
                .map(connectionRequest -> connectionService.addConnection(
                        connectionRequest.getIndex(),
                        connectionRequest.getName(),
                        trafficLightsIds
                                .stream()
                                .filter(trafficLightId -> connectionRequest.getTrafficLightIds().contains(trafficLightService.getTrafficLightById(trafficLightId).getIndex()))
                                .collect(Collectors.toList()),
                        roadIds.stream()
                                .filter(roadId -> roadService.getRoadById(roadId).getIndex() == connectionRequest.getSourceId())
                                .findAny()
                                .orElseThrow(),
                        roadIds.stream()
                                .filter(roadId -> roadService.getRoadById(roadId).getIndex() == connectionRequest.getTargetId())
                                .findAny()
                                .orElseThrow(),
                        Collections.emptyList()
                ).getId())
                .collect(Collectors.toList());

        List<String> collisionIds = crossroadDescription
                .getCollisions()
                .stream()
                .map(collisionRequest -> collisionService.addCollision(
                        collisionRequest.getIndex(),
                        collisionRequest.getName(),
                        connectionIds
                                .stream()
                                .filter(connectionId -> connectionService.getConnectionById(connectionId).getIndex() == collisionRequest.getConnection1Id())
                                .findAny()
                                .orElseThrow(),
                        connectionIds
                                .stream()
                                .filter(connectionId -> connectionService.getConnectionById(connectionId).getIndex() == collisionRequest.getConnection2Id())
                                .findAny()
                                .orElseThrow(),
                        collisionRequest.getBothCanBeOn()
                ).getId())
                .collect(Collectors.toList());

        String imageId = imageService.store(image);

        crossroadService.addCrossroad(
                crossroadDescription.getCrossroad().getName(),
                crossroadDescription.getCrossroad().getLocation(),
                creatorId,
                crossroadDescription.getCrossroad().getType(),
                roadIds,
                collisionIds,
                connectionIds,
                trafficLightsIds,
                imageId
        );

        return ResponseEntity
                .ok()
                .body(true);
    }

    @GetMapping(value = "/{crossroadId}/optimization/{videoId}/{time}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOptimization( // TODO: change to JSONObject
           @PathVariable String crossroadId,
           @PathVariable String videoId,
           @PathVariable int time
    ) {
        String result = "{}";
        try (Socket socket = new Socket(OPTIMIZER_HOST, OPTIMIZER_PORT)) {
            JSONObject jsonData = crossroadsUtils.getJsonData(crossroadId, time);
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
                sleep(time * 1000L);
                result = Files.readString(Paths.get("temp/newTemplateOutput.json"));
            } catch (Exception ignored) {
            }
        }
        System.out.println(result);

        return ResponseEntity
                .ok()
                .body(result);
    }

    @DeleteMapping(value = "/{crossroadId}")
    public ResponseEntity<Boolean> deleteCrossroad(
            @PathVariable String crossroadId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwtToken
    ) {
        String creatorId = jwtUtil.getId(
                jwtUtil.parseJwtClaims(
                        jwtToken.split(" ")[1]
                )
        );

        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        if (crossroad == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        } else if (crossroad.getCreatorId().equals(creatorId)) {
            return ResponseEntity
                    .status(UNAUTHORIZED)
                    .build();
        }

        for (String roadId : crossroad.getRoadIds()) {
            roadService.deleteRoadById(roadId);
        }

        for (String collisionId : crossroad.getCollisionIds()) {
            collisionService.deleteCollisionById(collisionId);
        }

        for (String connectionId : crossroad.getConnectionIds()) {
            connectionService.deleteConnectionById(connectionId);
        }

        for (String trafficLightId : crossroad.getTrafficLightIds()) {
            trafficLightService.deleteTrafficLightById(trafficLightId);
        }

        imageService.deleteImageById(crossroad.getImageId());

        crossroadService.deleteCrossroadById(crossroadId);

        return ResponseEntity
                .ok()
                .body(true);
    }

    private List<List<Integer>> convertJSONArrayToArray(String jsonArray) {
        JSONObject obj = new JSONObject(jsonArray);
        JSONArray arr = obj.getJSONArray("results");
        List<List<Integer>> ar = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < arr.getJSONArray(0).length(); j++) {
                row.add((int) arr.getJSONArray(i).get(j));
            }
            ar.add(row);
        }
        return ar;
    }

    @GetMapping(value = "/{crossroadId}/optimization/novid/socket/{time}", produces = MediaType.APPLICATION_JSON_VALUE)
    // TODO
    public String getOptimizationWithoutVideoSocket(@PathVariable String crossroadId, @PathVariable int time) {

        // TODO: use OptimizationRequest class and create OptimizationResponse class

        String result = "{results: \"ERROR\"}";
        try (Socket socket = new Socket(OPTIMIZER_HOST, OPTIMIZER_PORT)) {
            JSONObject jsonData = crossroadsUtils.getJsonData(crossroadId, time);
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
                sleep(time * 1000L);
                result = Files.readString(Paths.get("temp/newTemplateOutput.json"));
            } catch (Exception ignored) {
            }
        }
        System.out.println(result);

        return result;
    }


    @GetMapping(value = "/{crossroadId}/optimization/novid/{time}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOptimizationWithoutVideo(@PathVariable String crossroadId, @PathVariable int time) {
//        OptimizationRequest optimizationRequest = crossroadsUtils.getOptimizationRequest(crossroadId, time);
        OptimizationRequest optimizationRequest = new OptimizationRequest();

        String url = "http://localhost:9091/optimization";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OptimizationRequest> requestEntity = new HttpEntity<>(optimizationRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException exception) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid data given to optimizer\n");
        } catch (HttpServerErrorException exception) {
            return ResponseEntity.status(SERVICE_UNAVAILABLE).body("Optimizer unavailable\n");
        } catch (UnknownHttpStatusCodeException exception) {
            return ResponseEntity.status(NOT_FOUND).body("Something weird happened\n");
        }
        return response;
    }

    @GetMapping(value = "/{crossroadId}/optimization_results", produces = MediaType.APPLICATION_JSON_VALUE) // TODO
    public String getOptimizationResults(@PathVariable String crossroadId, String timeIntervalID) {
        try {
            List<List<Integer>> newestResult = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            List<List<Integer>> secondNewestResult = optimizationService.getSecondNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            return crossroadsUtils.parseOutput(newestResult, secondNewestResult, crossroadId);
        } catch (Exception e) {
            try {
                return Files.readString(Paths.get("temp/newTemplateOutput.json"));
            } catch (Exception ignored) {
                throw new RuntimeException("Error with reading template output.");
            }
        }
    }
}
