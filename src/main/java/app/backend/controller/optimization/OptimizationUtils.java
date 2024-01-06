package app.backend.controller.optimization;

import app.backend.document.Collision;
import app.backend.document.Connection;
import app.backend.document.Optimization;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;
import app.backend.document.road.Road;
import app.backend.document.time.Day;
import app.backend.document.time.Hour;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.response.optimization.OptimizationResultResponse;
import app.backend.service.CarFlowService;
import app.backend.service.CollisionService;
import app.backend.service.ConnectionService;
import app.backend.service.CrossroadService;
import app.backend.service.OptimizationService;
import app.backend.service.RoadService;
import app.backend.service.StartTimeService;
import app.backend.service.TrafficLightService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static app.backend.controller.optimization.OptimizationResultMock.LIGHT_BY_LIGHT;
import static app.backend.controller.optimization.OptimizationResultMock.RANDOM;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
public class OptimizationUtils {
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final TrafficLightService trafficLightService;
    private final ConnectionService connectionService;
    private final CarFlowService carFlowService;
    private final OptimizationService optimizationService;
    private final StartTimeService startTimeService;
    private final ObjectMapper objectMapper;
    @Value("${optimizer.optimization_time_period_scaling}")
    private final int OPTIMIZATION_TIME_PERIOD_SCALING = 3;
    @Value("${optimizer.host}")
    private String OPTIMIZER_HOST;
    @Value("${optimizer.port}")
    private int OPTIMIZER_PORT;
    @Value("${optimizer.password}")
    private String OT_PASSWORD;

    @Autowired
    public OptimizationUtils(
            CrossroadService crossroadService,
            RoadService roadService,
            CollisionService collisionService,
            TrafficLightService trafficLightService,
            ConnectionService connectionService,
            CarFlowService carFlowService,
            OptimizationService optimizationService,
            StartTimeService startTimeService,
            ObjectMapper objectMapper
    ) {
        this.crossroadService = crossroadService;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.trafficLightService = trafficLightService;
        this.connectionService = connectionService;
        this.carFlowService = carFlowService;
        this.optimizationService = optimizationService;
        this.startTimeService = startTimeService;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Void> orderOptimization(
            String crossroadId,
            int optimizationTime,
            Day day,
            Hour hour
    ) {
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);


        OptimizationRequest optimizationRequest;
        try {
            optimizationRequest = getOptimizationRequest(crossroadId, startTimeId, optimizationTime, OPTIMIZATION_TIME_PERIOD_SCALING);
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(optimizationRequest);
            requestBody = "{\"optimization_request\": " + requestBody + "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .uri(URI.create("http://" + OPTIMIZER_HOST + ":" + OPTIMIZER_PORT + "/optimization?password=" + OT_PASSWORD))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            addOptimizationResultsToDb(crossroadId, startTimeId, response);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 422) {
                return ResponseEntity
                        .status(UNPROCESSABLE_ENTITY)
                        .build();
            }
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .build();
        } catch (HttpServerErrorException | ResourceAccessException | IOException | InterruptedException exception) {
            return ResponseEntity
                    .status(SERVICE_UNAVAILABLE)
                    .build();
        } catch (UnknownHttpStatusCodeException exception) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
        return ResponseEntity
                .status(OK)
                .build();
    }

    public ResponseEntity<Void> addTrafficLightsCycles(MultipartFile file, String crossroadId) {
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        if (crossroad == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        int version = optimizationService.getFreeVersionNumber(crossroadId);
        List<String> startTimeIds = startTimeService.getAllStartTimeIds();
        int trafficLightsCount = crossroad.getTrafficLightIds().size();
        List<List<Integer>> trafficLightsCycles;
        try {
            trafficLightsCycles = parseTrafficLightsCyclesFile(file, trafficLightsCount);
        } catch (IOException e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .build();
        }

        startTimeIds.forEach(startTimeId ->
                optimizationService.addOptimization(
                        crossroadId,
                        version,
                        startTimeId,
                        trafficLightsCycles
                )
        );

        return ResponseEntity
                .ok()
                .build();
    }

    public ResponseEntity<Void> addTrafficLightsCycles(MultipartFile file, String crossroadId, Day day, Hour hour) {
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        if (crossroad == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        int version = optimizationService.getFreeVersionNumber(crossroadId);
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);
        int trafficLightsCount = crossroad.getTrafficLightIds().size();
        List<List<Integer>> trafficLightsCycles;
        try {
            trafficLightsCycles = parseTrafficLightsCyclesFile(file, trafficLightsCount);
        } catch (IOException e) {
            return ResponseEntity
                    .status(INTERNAL_SERVER_ERROR)
                    .build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .build();
        }

        optimizationService.addOptimization(
                crossroadId,
                version,
                startTimeId,
                trafficLightsCycles
        );

        return ResponseEntity
                .ok()
                .build();
    }

    private List<List<Integer>> parseTrafficLightsCyclesFile(
            MultipartFile file,
            int trafficLightsCount
    ) throws IOException, HttpClientErrorException {
        List<List<Integer>> trafficLightsCycles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int linesCount = 0;
            while ((line = br.readLine()) != null) {
                linesCount++;
                String[] values = line.split(",");
                if (values.length != 60) {
                    throw new HttpClientErrorException(BAD_REQUEST);
                }

                trafficLightsCycles.add(
                        Arrays.stream(values)
                                .map(Integer::valueOf)
                                .toList()
                );
            }
            if (linesCount != trafficLightsCount) {
                throw new HttpClientErrorException(BAD_REQUEST);
            }
        }

        return trafficLightsCycles;
    }

    private OptimizationRequest getOptimizationRequest(String crossroadId, String startTimeId, int time, int scaling) {
        OptimizationRequest optimizationRequest = new OptimizationRequest();

        optimizationRequest.setOptimizationTime(time);

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);

            if (crossroad == null) {
                throw new EntityNotFoundException();
            }

            //  -----------------------------  roads  -----------------------------

            List<Integer> roadCapacities = crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById)
                    .sorted(Comparator.comparingInt(Road::getIndex))
                    .map(road -> {
                        return road.getCapacity() == -1 ? 0 : road.getCapacity();
                    })
                    .toList();

            optimizationRequest.setRoadCapacities(roadCapacities);
            optimizationRequest.setRoadCount(roadCapacities.size());

            //  -----------------------------  collisions  -----------------------------

            List<Integer> isCollisionImportant = crossroad
                    .getCollisionIds()
                    .stream()
                    .map(collisionService::getCollisionById)
                    .sorted(Comparator.comparingInt(Collision::getIndex))
                    .map(collision -> collision.getBothCanBeOn() ? 0 : 1)
                    .toList();

            List<List<Integer>> CollisionConnections = crossroad
                    .getCollisionIds()
                    .stream()
                    .map(collisionService::getCollisionById)
                    .sorted(Comparator.comparingInt(Collision::getIndex))
                    .map(collision -> {
                        String connection1Id = collision.getConnection1Id();
                        String connection2Id = collision.getConnection2Id();

                        Connection connection1 = connectionService.getConnectionById(connection1Id);
                        Connection connection2 = connectionService.getConnectionById(connection2Id);
                        return Arrays.asList(connection1.getIndex(), connection2.getIndex());
                    })
                    .toList();

            optimizationRequest.setIsCollisionImportant(isCollisionImportant);
            optimizationRequest.setCollisionConnections(CollisionConnections);
            optimizationRequest.setCollisionCount(CollisionConnections.size());

            //  ------------------------  road connections lights  ------------------------

            List<String> connections = crossroad.getConnectionIds();
            List<Road> roads = crossroad.getRoadIds().stream().map(roadService::getRoadById).toList();

            HashMap<Integer, List<Integer>> roadConnectionsInMap = new HashMap<>();
            HashMap<Integer, List<Integer>> roadConnectionsOutMap = new HashMap<>();
            List<Integer> isConnectionFromIntermediate = new ArrayList<>();

            connections.stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .forEach(connection -> {
                        isConnectionFromIntermediate.add(roadService.getRoadById(connection.getSourceId()).getCapacity() == -1 ? 0 : 1);

                        int targetIdx = roadService.getRoadById(connection.getTargetId()).getIndex();
                        if (roadConnectionsInMap.containsKey(targetIdx)) {
                            List<Integer> currentConnections = new ArrayList<>();
                            currentConnections.addAll(roadConnectionsInMap.get(targetIdx));
                            currentConnections.add(connection.getIndex());
                            roadConnectionsInMap.put(targetIdx, currentConnections);
                        } else {
                            roadConnectionsInMap.put(targetIdx, List.of(connection.getIndex()));
                        }
                        int sourceIdx = roadService.getRoadById(connection.getSourceId()).getIndex();
                        if (roadConnectionsOutMap.containsKey(sourceIdx)) {
                            List<Integer> currentConnections = new ArrayList<>();
                            currentConnections.addAll(roadConnectionsOutMap.get(sourceIdx));
                            currentConnections.add(connection.getIndex());
                            roadConnectionsOutMap.put(sourceIdx, currentConnections);
                        } else {
                            roadConnectionsOutMap.put(sourceIdx, List.of(connection.getIndex()));
                        }
                    });

            List<List<Integer>> roadConnectionsIn = new ArrayList<>();
            for (Road road : roads) {
                List<Integer> newCons = new ArrayList<>();
                if (roadConnectionsInMap.containsKey(road.getIndex())) {
                    newCons = new ArrayList<>(roadConnectionsInMap.get(road.getIndex()));
                }
                while (newCons.size() < 3) {
                    newCons.add(0);
                }
                roadConnectionsIn.add(newCons);
            }

            List<List<Integer>> roadConnectionsOut = new ArrayList<>();
            for (Road road : roads) {
                List<Integer> newCons = new ArrayList<>();
                if (roadConnectionsOutMap.containsKey(road.getIndex())) {
                    newCons = new ArrayList<>(roadConnectionsOutMap.get(road.getIndex()));
                }
                while (newCons.size() < 3) {
                    newCons.add(0);
                }
                roadConnectionsOut.add(newCons);
            }

            optimizationRequest.setIsConnectionFromIntermediate(isConnectionFromIntermediate);

            optimizationRequest.setRoadConnectionsIn(roadConnectionsIn);
            optimizationRequest.setRoadConnectionsOut(roadConnectionsOut);
            optimizationRequest.setConnectionCount(connections.size());

            //  -----------------------------  car flow  -----------------------------

            List<Integer> carFlows = new ArrayList<>(connections
                    .stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .map(connection -> {
                        try {
                            return carFlowService.getNewestCarFlowByStartTimeIdForConnection(connection.getId(), startTimeId).getCarFlow();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).toList());

            optimizationRequest.setExpectedCarFlow(carFlows);

            //  -----------------------------  lights  -----------------------------

            List<List<Integer>> connectionsLights = new ArrayList<>(connections
                    .stream()
                    .map(connectionService::getConnectionById)
                    .sorted(Comparator.comparingInt(Connection::getIndex))
                    .map(connection -> {
                        List<Integer> lights = new ArrayList<>(connection.getTrafficLightIds()
                                .stream()
                                .map(trafficLightService::getTrafficLightById)
                                .map(TrafficLight::getIndex)
                                .toList());
                        while (lights.size() < 3) {
                            lights.add(0);
                        }
                        return lights;
                    })
                    .toList());

            List<String> lights = crossroad.getTrafficLightIds();

            optimizationRequest.setConnectionLights(connectionsLights);
            optimizationRequest.setLightCount(lights.size());

            //  -----------------------------  fixed values  -----------------------------

            optimizationRequest.setTimeUnitsInMinute(60);
            optimizationRequest.setTimeUnitCount(60);
            optimizationRequest.setScaling(scaling);

            List<TrafficLightDirection> setLightsType = crossroad.getTrafficLightIds().stream()
                    .map(trafficLightService::getTrafficLightById)
                    .map(TrafficLight::getDirection)
                    .toList();
            optimizationRequest.setLightsTypes(setLightsType);

            //  -----------------------------  previous results  -----------------------------

            Optimization previousOptimization = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, startTimeId);
            if (previousOptimization == null) {
                optimizationRequest.setPreviousResults(null);
            } else {
                optimizationRequest.setPreviousResults(previousOptimization.getResults());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimizationRequest;
    }

    private void mockResponseToDb(String crossroadId, String startTimeId, OptimizationResultMock mockVersion) {
        switch (mockVersion) {
            case RANDOM -> mockRandom(crossroadId, startTimeId);
            case LIGHT_BY_LIGHT -> mockLightByLight(crossroadId, startTimeId);
        }
    }

    private void mockRandom(String crossroadId, String startTimeId) {
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        for (int i = 0; i < crossroad.getTrafficLightIds().size(); i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 60; j++) {
                double random = Math.random();
                if (random < 0.25) {
                    list.add(1);
                } else {
                    list.add(0);
                }
            }
            sequences.add(list);
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                sequences
        );
    }

    private void mockLightByLight(String crossroadId, String startTimeId) {
        List<List<Integer>> sequences = new ArrayList<>();
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
        int lightInterval = (int) Math.floor(60.0 / crossroad.getTrafficLightIds().size());
        int lightCount = crossroad.getTrafficLightIds().size();
        for (int i = 0; i < lightCount; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 60; j++) {
                if (lightInterval * i <= j && j < lightInterval * (i + 1)) {
                    list.add(1);
                } else {
                    list.add(0);
                }
            }
            sequences.add(list);
        }

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                sequences
        );
    }

    private void addOptimizationResultsToDb(
            String crossroadId,
            String startTimeId,
            HttpResponse<String> result
    ) throws JsonProcessingException {
        List<List<Integer>> resultList = objectMapper.readValue(result.body(), new TypeReference<>() {
        });

        optimizationService.addOptimization(
                crossroadId,
                optimizationService.getFreeVersionNumber(crossroadId),
                startTimeId,
                resultList
        );
    }

    public ResponseEntity<OptimizationResultResponse> retrieveOptimizationResult(
            String crossroadId,
            Day day,
            Hour hour
    ) {
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);

        HashMap<Integer, List<Integer>> lightsSequenceMapCurrent = new HashMap<>();
        HashMap<Integer, Double> connectionsFlowRatioMapCurrent = new HashMap<>();
        HashMap<Integer, List<Integer>> lightsSequenceMapPrevious = new HashMap<>();
        HashMap<Integer, Double> connectionsFlowRatioMapPrevious = new HashMap<>();

        HashMap<Integer, List<TrafficLight>> connectionsLightsMap = new HashMap<>();
        HashMap<Integer, List<TrafficLight>> roadsLightsMap = new HashMap<>();
        HashMap<Integer, TrafficLightDirection> lightsDirectionMap = new HashMap<>();
        HashMap<Integer, Integer> connectionsFlowMap = carFlowService.getConnectionIdxToCurrentCarFlowMapByStartTimeIdForCrossroad(crossroadId, startTimeId);
        HashMap<Integer, Integer> connectionsRoadMap = new HashMap<>();
        HashMap<Integer, Double> roadsFlowMap = new HashMap<>();
        HashMap<Integer, Double> connectionChanceToPickMap = new HashMap<>();

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
            if (crossroad == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }

            Optimization optimizationCurrent = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, startTimeId);
            Optimization optimizationPrevious = optimizationService.getSecondNewestOptimizationByCrossroadId(crossroadId, startTimeId);
            if (optimizationCurrent == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }

//            getTrafficLightIds

            List<List<Integer>> resultCurrent = optimizationCurrent.getResults();

            List<TrafficLight> trafficLightStream = crossroad.getTrafficLightIds()
                    .stream()
                    .map(trafficLightService::getTrafficLightById).toList();
            trafficLightStream.forEach(trafficLight -> {
                        lightsSequenceMapCurrent.put(
                                trafficLight.getIndex(),
                                resultCurrent.get(trafficLight.getIndex() - 1)
                        );
                        lightsDirectionMap.put(trafficLight.getIndex(), trafficLight.getDirection());
                    }
            );

//            getConnectionIds
            List<Connection> connectionStream = crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById).toList();
            connectionStream.forEach(connection ->
                    {
                        connectionsFlowRatioMapCurrent.put(
                                connection.getIndex(),
                                connection.getTrafficLightIds()
                                        .stream()
                                        .map(trafficLightId ->
                                                lightsSequenceMapCurrent.get(trafficLightService.getTrafficLightById(trafficLightId).getIndex())
                                                        .stream()
                                                        .mapToInt(Integer::intValue)
                                                        .sum())
                                        .mapToInt(Integer::intValue)
                                        .sum() / (double) connectionsFlowMap.get(connection.getIndex())
                        );
                        connectionsLightsMap.put(
                                connection.getIndex(),
                                connection.getTrafficLightIds()
                                        .stream()
                                        .map(trafficLightService::getTrafficLightById).toList()
                        );
                        connectionsRoadMap.put(connection.getIndex(), roadService.getRoadById(connection.getSourceId()).getIndex());
                    }
            );

//            getRoadIds

            List<Road> roadStream = crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById).toList();
            roadStream.forEach(road -> {
                        roadsLightsMap.put(
                                road.getIndex(),
                                crossroad.getConnectionIds()
                                        .stream()
                                        .map(connectionService::getConnectionById)
                                        .filter(connection -> connection.getSourceId().equals(road.getId()))
                                        .map(Connection::getTrafficLightIds)
                                        .flatMap(List::stream)
                                        .distinct()
                                        .map(trafficLightService::getTrafficLightById)
                                        .toList()
                        );
                        double summedFlow = connectionService.getConnectionsOutByRoadId(crossroadId, road.getId())
                                .stream()
                                .map(connection -> connectionsFlowMap.get(connection.getIndex()))
                                .mapToInt(Integer::intValue)
                                .sum();
                        roadsFlowMap.put(road.getIndex(), summedFlow);
                        HashMap<Integer, Integer> connectionCarFlowMap = new HashMap<>();
                        connectionService.getConnectionsOutByRoadId(crossroadId, road.getId())
                                .forEach(connection ->
                                        connectionCarFlowMap.put(
                                                connection.getIndex(),
                                                connectionsFlowMap.get(connection.getIndex())
                                        )
                                );
                        for (int connectionIndex : connectionCarFlowMap.keySet()) {
                            connectionChanceToPickMap.put(
                                    connectionIndex,
                                    connectionCarFlowMap.get(connectionIndex) / (double) roadsFlowMap.get(road.getIndex()));
                        }
                    }
            );

            if (optimizationPrevious != null) {
                List<List<Integer>> resultPrevious = optimizationPrevious.getResults();

                trafficLightStream.forEach(trafficLight -> lightsSequenceMapPrevious.put(
                                trafficLight.getIndex(),
                                resultPrevious.get(trafficLight.getIndex() - 1)
                        )
                );
                connectionStream.forEach(connection -> connectionsFlowRatioMapPrevious.put(
                                connection.getIndex(),
                                connection.getTrafficLightIds()
                                        .stream()
                                        .map(trafficLightId ->
                                                lightsSequenceMapPrevious.get(trafficLightService.getTrafficLightById(trafficLightId).getIndex())
                                                        .stream()
                                                        .mapToInt(Integer::intValue)
                                                        .sum())
                                        .mapToInt(Integer::intValue)
                                        .sum() / (double) connectionsFlowMap.get(connection.getIndex())
                        )
                );
            }

        } catch (Exception exception) {
            System.out.println(exception);
            return ResponseEntity
                    .status(EXPECTATION_FAILED)
                    .body(new OptimizationResultResponse(
                            lightsSequenceMapCurrent,
                            connectionsFlowRatioMapCurrent,
                            lightsSequenceMapPrevious,
                            connectionsFlowRatioMapPrevious,
                            connectionsLightsMap,
                            roadsLightsMap,
                            lightsDirectionMap,
                            connectionsFlowMap,
                            connectionsRoadMap,
                            roadsFlowMap,
                            connectionChanceToPickMap)
                    );
        }

        return ResponseEntity
                .ok()
                .body(new OptimizationResultResponse(
                        lightsSequenceMapCurrent,
                        connectionsFlowRatioMapCurrent,
                        lightsSequenceMapPrevious,
                        connectionsFlowRatioMapPrevious,
                        connectionsLightsMap,
                        roadsLightsMap,
                        lightsDirectionMap,
                        connectionsFlowMap,
                        connectionsRoadMap,
                        roadsFlowMap,
                        connectionChanceToPickMap)
                );
    }
}
