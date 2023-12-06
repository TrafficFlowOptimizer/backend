package app.backend.controller.optimization;

import app.backend.document.Connection;
import app.backend.document.Optimization;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static app.backend.controller.optimization.OptimizationResultMock.LIGHT_BY_LIGHT;
import static app.backend.controller.optimization.OptimizationResultMock.RANDOM;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;


@RestController
@RequestMapping("/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final ConnectionService connectionService;
    private final TrafficLightService trafficLightService;
    private final CarFlowService carFlowService;
    private final StartTimeService startTimeService;
    private final OptimizationUtils optimizationUtils;
    @Value("${optimizer.host}")
    private String OPTIMIZER_HOST;
    @Value("${optimizer.port}")
    private int OPTIMIZER_PORT;
    @Value("${optimizer.optimization_time_period_scaling}")
    private final int OPTIMIZATION_TIME_PERIOD_SCALING = 3;

    @Autowired
    public OptimizationController(
            OptimizationService optimizationService,
            CrossroadService crossroadService,
            OptimizationUtils optimizationUtils,
            RoadService roadService,
            CollisionService collisionService,
            ConnectionService connectionService,
            TrafficLightService trafficLightService,
            StartTimeService startTimeService,
            CarFlowService carFlowService
    ) {
        this.optimizationService = optimizationService;
        this.crossroadService = crossroadService;
        this.optimizationUtils = optimizationUtils;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.connectionService = connectionService;
        this.trafficLightService = trafficLightService;
        this.startTimeService = startTimeService;
        this.carFlowService = carFlowService;

    }

//    @GetMapping(value = "/{optimizationId}")
//    public ResponseEntity<Optimization> getOptimization(@PathVariable String optimizationId) {
//        Optimization optimization = optimizationService.getOptimizationById(optimizationId);
//
//        if (optimization != null) {
//            return ResponseEntity
//                    .ok()
//                    .body(optimization);
//        } else {
//            return ResponseEntity
//                    .status(NOT_FOUND)
//                    .build();
//        }
//    }
//
//    @GetMapping(value = "/{crossroadId}")
//    public ResponseEntity<List<Optimization>> list(@PathVariable String crossroadId) {
//        Iterable<Optimization> optimizations = optimizationService.getOptimizationsByCrossroadId(crossroadId);
//
//        List<Optimization> ret = new LinkedList<>();
//        for (Optimization optimization : optimizations) {
//            ret.add(optimization);
//        }
//
//        return ResponseEntity
//                .ok()
//                .body(ret);
//    }

    @PostMapping(value = "/{crossroadId}")
    public ResponseEntity<Void> orderOptimization(
            @PathVariable String crossroadId,
            @RequestParam int optimizationTime,
            @RequestParam Day day,
            @RequestParam Hour hour
    ) {
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);

        //TODO: mocked optimizer FOR DEVELOPMENT ONLY!
        boolean mocked = true;
        OptimizationResultMock optimizationResultMock = null;
        switch (optimizationTime) {
            case 1 -> optimizationResultMock = RANDOM;
            case -1 -> mocked = false;
            default -> optimizationResultMock = LIGHT_BY_LIGHT;
        }
        if (mocked) {
            optimizationUtils.mockResponseToDb(crossroadId, startTimeId, optimizationResultMock);
            return ResponseEntity
                    .status(OK).build();
        }

        OptimizationRequest optimizationRequest;
        try {
            optimizationRequest = optimizationUtils.getOptimizationRequest(crossroadId, startTimeId, optimizationTime, OPTIMIZATION_TIME_PERIOD_SCALING);
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }

        String url = "http://" + OPTIMIZER_HOST + ":" + OPTIMIZER_PORT + "/optimization";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OptimizationRequest> requestEntity = new HttpEntity<>(optimizationRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            optimizationUtils.addOptimizationResultsToDb(crossroadId, startTimeId, response);
        } catch (HttpClientErrorException exception) {
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .build();
        } catch (HttpServerErrorException exception) {
            if (exception.getStatusCode().value() == 515) {
                return ResponseEntity
                        .status(515)
                        .build();
            }
            return ResponseEntity
                    .status(SERVICE_UNAVAILABLE)
                    .build();
        } catch (JsonProcessingException | ResourceAccessException exception) {
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


    @GetMapping(value = "/result/{crossroadId}")
    public ResponseEntity<OptimizationResultResponse> getOptimizationResult(
            @PathVariable String crossroadId,
            @RequestParam Day day,
            @RequestParam Hour hour
    ) {
        String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);

        HashMap<Integer, List<Integer>> lightsSequenceMapCurrent = new HashMap<>();
        HashMap<Integer, Double> connectionsFlowRatioMapCurrent = new HashMap<>();
        HashMap<Integer, List<Integer>> lightsSequenceMapPrevious = new HashMap<>();
        HashMap<Integer, Double> connectionsFlowRatioMapPrevious = new HashMap<>();

        HashMap<Integer, List<TrafficLight>> connectionsLightsMap = new HashMap<>();
        HashMap<Integer, List<TrafficLight>> roadsLightsMap = new HashMap<>();
        HashMap<Integer, TrafficLightDirection> lightsDirectionMap = new HashMap<>();
        HashMap<Integer, Double> connectionsFlowMap = new HashMap<>();
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


            //  -----------------------------  connectionsFlowMap  -----------------------------

            crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection ->
                            connectionsFlowMap.put(
                                    connection.getIndex(),
                                    (double) carFlowService.getNewestCarFlowByStartTimeIdForConnection(connection.getId(), startTimeId).getCarFlow()
                            )
                    );

            //  -----------------------------  lightsSequenceMapCurrent  -----------------------------

            Optimization optimizationCurrent = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, startTimeId);
            if (optimizationCurrent == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }
            List<List<Integer>> resultCurrent = optimizationCurrent.getResults();

            crossroad.getTrafficLightIds()
                    .stream()
                    .map(trafficLightService::getTrafficLightById)
                    .forEach(trafficLight -> lightsSequenceMapCurrent.put(
                                    trafficLight.getIndex(),
                                    resultCurrent.get(trafficLight.getIndex() - 1)
                            )
                    );

            //  -----------------------------  connectionsFlowRatioMapCurrent  -----------------------------

            crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection -> connectionsFlowRatioMapCurrent.put(
                                    connection.getIndex(),
                                    connection.getTrafficLightIds()
                                            .stream()
                                            .map(trafficLightId ->
                                                    lightsSequenceMapCurrent.get(trafficLightService.getTrafficLightById(trafficLightId).getIndex())
                                                            .stream()
                                                            .mapToInt(Integer::intValue)
                                                            .sum())
                                            .mapToInt(Integer::intValue)
                                            .sum() / connectionsFlowMap.get(connection.getIndex())
                            )
                    );

            //  -----------------------------  lightsSequenceMapPrevious  -----------------------------

            Optimization optimizationPrevious = optimizationService.getSecondNewestOptimizationByCrossroadId(crossroadId, startTimeId);
            if (optimizationPrevious != null) {
                List<List<Integer>> resultPrevious = optimizationPrevious.getResults();

                crossroad.getTrafficLightIds()
                        .stream()
                        .map(trafficLightService::getTrafficLightById)
                        .forEach(trafficLight -> lightsSequenceMapPrevious.put(
                                        trafficLight.getIndex(),
                                        resultPrevious.get(trafficLight.getIndex() - 1)
                                )
                        );
            }

            //  -----------------------------  connectionsFlowRatioMapPrevious  -----------------------------

            if (optimizationPrevious != null) {
                crossroad.getConnectionIds()
                        .stream()
                        .map(connectionService::getConnectionById)
                        .forEach(connection -> connectionsFlowRatioMapPrevious.put(
                                        connection.getIndex(),
                                        connection.getTrafficLightIds()
                                                .stream()
                                                .map(trafficLightId ->
                                                        lightsSequenceMapPrevious.get(trafficLightService.getTrafficLightById(trafficLightId).getIndex())
                                                                .stream()
                                                                .mapToInt(Integer::intValue)
                                                                .sum())
                                                .mapToInt(Integer::intValue)
                                                .sum() / connectionsFlowMap.get(connection.getIndex())
                                )
                        );
            }

            //  -----------------------------  connectionsLightsMap  -----------------------------

            crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection -> connectionsLightsMap.put(
                                    connection.getIndex(),
                                    connection.getTrafficLightIds()
                                            .stream()
                                            .map(trafficLightService::getTrafficLightById).toList()
                            )
                    );

            //  -----------------------------  roadsLightsMap  -----------------------------

            crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById)
                    .forEach(road -> roadsLightsMap.put(
                                    road.getIndex(),
                                    crossroad.getConnectionIds()
                                            .stream()
                                            .map(connectionService::getConnectionById)
                                            .filter(connection -> Objects.equals(connection.getSourceId(), road.getId()))
                                            .map(Connection::getTrafficLightIds)
                                            .flatMap(List::stream)
                                            .distinct()
                                            .map(trafficLightService::getTrafficLightById)
                                            .toList()
                            )
                    );

            //  -----------------------------  lightsDirectionMap  -----------------------------

            crossroad.getTrafficLightIds()
                    .stream()
                    .map(trafficLightService::getTrafficLightById)
                    .forEach(trafficLight -> lightsDirectionMap.put(trafficLight.getIndex(), trafficLight.getDirection()));

            //  -----------------------------  connectionsRoadMap  -----------------------------

            crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection -> connectionsRoadMap.put(connection.getIndex(), roadService.getRoadById(connection.getSourceId()).getIndex()));

            //  -----------------------------  roadsFlowMap  -----------------------------

            crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById)
                    .forEach(road -> {
                        double summedFlow = connectionService.getConnectionsOutByRoadId(crossroadId, road.getId())
                                .stream()
                                .map(connection ->
                                        carFlowService.getNewestCarFlowByStartTimeIdForConnection(
                                                connection.getId(),
                                                startTimeId
                                        ).getCarFlow()
                                )
                                .mapToInt(Integer::intValue)
                                .sum();
                        roadsFlowMap.put(road.getIndex(), summedFlow);
                    });

            //  -----------------------------  connectionChanceToPickMap  -----------------------------

            crossroad.getRoadIds()
                    .stream()
                    .map(roadService::getRoadById)
                    .forEach(road -> {
                        HashMap<Integer, Double> connectionCarFlowMap = new HashMap<>();
                        connectionService.getConnectionsOutByRoadId(crossroadId, road.getId())
                                .forEach(connection ->
                                        connectionCarFlowMap.put(
                                                connection.getIndex(),
                                                (double) carFlowService.getNewestCarFlowByStartTimeIdForConnection(
                                                        connection.getId(),
                                                        startTimeId
                                                ).getCarFlow()
                                        )
                                );
                        for (int connectionIndex : connectionCarFlowMap.keySet()) {
                            connectionChanceToPickMap.put(
                                    connectionIndex,
                                    connectionCarFlowMap.get(connectionIndex) / roadsFlowMap.get(road.getIndex()));
                        }
                    });

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
