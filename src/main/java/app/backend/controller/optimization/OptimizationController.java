package app.backend.controller.optimization;

import app.backend.document.Connection;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

        boolean mocked = false;//TODO: mocked optimizer FOR DEVELOPMENT ONLY!
        if (mocked) {
            optimizationUtils.mockResponseToDb(crossroadId, startTimeId);
            return ResponseEntity
                    .status(OK).build();
        }

        OptimizationRequest optimizationRequest;
        try {
            optimizationRequest = optimizationUtils.getOptimizationRequest(crossroadId, startTimeId, optimizationTime);
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
//        OptimizationRequest optimizationRequest = new OptimizationRequest();

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
        } catch (HttpServerErrorException | JsonProcessingException exception) {
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
        HashMap<Integer, List<Integer>> lightsSequenceMapPrevious = null;
        HashMap<Integer, Double> connectionsFlowRatioMapPrevious = null;

        HashMap<Integer, List<TrafficLight>> connectionsLightsMap = new HashMap<>();
        HashMap<Integer, List<TrafficLight>> roadsLightsMap = new HashMap<>();
        HashMap<Integer, TrafficLightType> lightsDirectionMap = new HashMap<>();

        try {
            Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
            if (crossroad == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }

            HashMap<String, Double> connectionFlowMap = new HashMap<>();

            crossroad.getConnectionIds()
                    .stream()
                    .map(connectionService::getConnectionById)
                    .forEach(connection ->
                            connectionFlowMap.put(
                                    connection.getId(),
                                    carFlowService.getNewestCarFlowByStartTimeIdForConnection(connection.getId(), startTimeId).getCarFlow()
                            )
                    );

            //  -----------------------------  lightsSequenceMapCurrent  -----------------------------

            List<List<Integer>> result = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, startTimeId).getResults();

            crossroad.getTrafficLightIds()
                    .stream()
                    .map(trafficLightService::getTrafficLightById)
                    .forEach(trafficLight -> lightsSequenceMapCurrent.put(
                                    trafficLight.getIndex(),
                                    result.get(trafficLight.getIndex() - 1)
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
                                            .sum() / connectionFlowMap.get(connection.getId())
                            )
                    );

            //  -----------------------------  lightsSequenceMapPrevious  -----------------------------//TODO

//        List<List<Integer>> result = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, startTime).getResults();
//
//        crossroad.getTrafficLightIds()
//                .stream()
//                .map(trafficLightService::getTrafficLightById)
//                .forEach(trafficLight -> lightsSequenceMapCurrent.put(
//                        trafficLight.getIndex(),
//                        result.get(trafficLight.getIndex())
//                        )
//                );

            //  -----------------------------  connectionsFlowRatioMapPrevious  -----------------------------//TODO

//        crossroad.getConnectionIds()
//                .stream()
//                .map(connectionService::getConnectionById)
//                .forEach(connection -> connectionsFlowRatioMapCurrent.put(
//                        connection.getIndex(),
//                        connection.getTrafficLightIds()
//                                .stream()
//                                .map(trafficLightId ->
//                                        lightsSequenceMapCurrent.get(trafficLightId)
//                                                .stream()
//                                                .mapToInt(Integer::intValue)
//                                                .sum())
//                                .mapToInt(Integer::intValue)
//                                .sum()/connectionFlowMap.get(connection.getId())
//                        )
//                );

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
                            lightsDirectionMap)
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
                        lightsDirectionMap)
                );
    }
}
