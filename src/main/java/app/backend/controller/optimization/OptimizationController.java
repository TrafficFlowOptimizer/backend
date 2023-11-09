package app.backend.controller.optimization;

import app.backend.controller.crossroad.CrossroadsUtils;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.response.optimization.OptimizationResultResponse;
import app.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/optimization")
public class OptimizationController {

    @Value("${optimizer.host}")
    private String OPTIMIZER_HOST;
    @Value("${optimizer.port}")
    private int OPTIMIZER_PORT;
    private final OptimizationService optimizationService;
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final ConnectionService connectionService;
    private final TrafficLightService trafficLightService;
    private final CarFlowService carFlowService;
    private final OptimizationUtils optimizationUtils;
    private final CrossroadsUtils crossroadsUtils;


    @Autowired
    public OptimizationController(OptimizationService optimizationService,
                                  CrossroadService crossroadService,
                                  OptimizationUtils optimizationUtils,
                                  CrossroadsUtils crossroadsUtils,
                                  RoadService roadService,
                                  CollisionService collisionService,
                                  ConnectionService connectionService,
                                  TrafficLightService trafficLightService,
                                  CarFlowService carFlowService) {
        this.optimizationService = optimizationService;
        this.crossroadService = crossroadService;
        this.optimizationUtils = optimizationUtils;
        this.crossroadsUtils = crossroadsUtils;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.connectionService = connectionService;
        this.trafficLightService = trafficLightService;
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


    @PostMapping(value = "/{crossroadId}/{optimizationTime}")
    public ResponseEntity<String> orderOptimization(@PathVariable String crossroadId, @PathVariable int optimizationTime){
        OptimizationRequest optimizationRequest;
        try {
            optimizationRequest = crossroadsUtils.getOptimizationRequest(crossroadId, optimizationTime);
        }
        catch (EntityNotFoundException e){
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
//        OptimizationRequest optimizationRequest = new OptimizationRequest();

        String url = "http://" + OPTIMIZER_HOST + "/" + OPTIMIZER_PORT;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OptimizationRequest> requestEntity = new HttpEntity<>(optimizationRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            //TODO: save response to DB
        } catch (HttpClientErrorException exception) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid data given to optimizer");
        } catch (HttpServerErrorException exception) {
            return ResponseEntity.status(SERVICE_UNAVAILABLE).body("Optimizer unavailable");
        } catch (UnknownHttpStatusCodeException exception) {
            return ResponseEntity.status(NOT_FOUND).body("Something weird happened");
        }
        return ResponseEntity.status(OK).body("Optimization completed successfully!");
    }


    @GetMapping(value = "/result/{crossroadId}/{timeInterval}")//TODO: add previous results to response
    public ResponseEntity<OptimizationResultResponse> getOptimizationResult(@PathVariable String crossroadId, @PathVariable String timeInterval){
        HashMap<String, List<Integer>> lightsSequenceMapCurrent = new HashMap<>();
        HashMap<String, Double> connectionsFlowRatioMapCurrent = new HashMap<>();
        HashMap<String, List<Integer>> lightsSequenceMapPrevious = null;
        HashMap<String, Double> connectionsFlowRatioMapPrevious = null;
        HashMap<String, List<TrafficLight>> connectionsLightsMap = new HashMap<>();
        HashMap<String, TrafficLightType> lightsDirectionMap = new HashMap<>();

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
                                connectionService.getNewestCarFlowByTimeIntervalIdForConnection(connection.getId(), timeInterval).getCarFlow()
                        )
                );

        //  -----------------------------  lightsSequenceMapCurrent  -----------------------------

        crossroad.getTrafficLightIds()
                .stream()
                .map(trafficLightService::getTrafficLightById)
                .forEach(trafficLight -> lightsSequenceMapCurrent.put(
                        trafficLight.getId(),
                        Arrays.asList(0, 0, 0, 1, 1, 1) //TODO: correct retrieving sequences
                        )
                );

        //  -----------------------------  connectionsFlowRatioMapCurrent  -----------------------------

        crossroad.getConnectionIds()
                .stream()
                .map(connectionService::getConnectionById)
                .forEach(connection -> connectionsFlowRatioMapCurrent.put(
                        connection.getId(),
                        connection.getTrafficLightIds()
                                .stream()
                                .map(trafficLightId ->
                                        lightsSequenceMapCurrent.get(trafficLightId)
                                                .stream()
                                                .mapToInt(Integer::intValue)
                                                .sum())
                                .mapToInt(Integer::intValue)
                                .sum()/connectionFlowMap.get(connection.getId())
                        )
                );

        //  -----------------------------  connectionsLightsMap  -----------------------------

        crossroad.getConnectionIds()
                .stream()
                .map(connectionService::getConnectionById)
                .forEach(connection -> connectionsLightsMap.put(
                        connection.getId(),
                        connection.getTrafficLightIds()
                                    .stream()
                                    .map(trafficLightService::getTrafficLightById).toList()
                        )
                );

        //  -----------------------------  lightsDirectionMap  -----------------------------

        crossroad.getTrafficLightIds()
                .stream()
                .map(trafficLightService::getTrafficLightById)
                .forEach(trafficLight -> lightsDirectionMap.put(trafficLight.getId(), trafficLight.getDirection()));

        return ResponseEntity
                .ok()
                .body(new OptimizationResultResponse(lightsSequenceMapCurrent,
                        connectionsFlowRatioMapCurrent,
                        lightsSequenceMapPrevious,
                        connectionsFlowRatioMapPrevious,
                        connectionsLightsMap,
                        lightsDirectionMap));
    }
}
