package app.backend.controller.crossroad;

import app.backend.authentication.JwtUtil;
import app.backend.document.crossroad.Crossroad;
import app.backend.document.time.Day;
import app.backend.document.time.Hour;
import app.backend.request.crossroad.CrossroadDescriptionRequest;
import app.backend.response.crossroad.CrossroadDescriptionResponse;
import app.backend.service.CarFlowService;
import app.backend.service.CollisionService;
import app.backend.service.ConnectionService;
import app.backend.service.CrossroadService;
import app.backend.service.ImageService;
import app.backend.service.OptimizationService;
import app.backend.service.RoadService;
import app.backend.service.StartTimeService;
import app.backend.service.TrafficLightService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@RestController
@CrossOrigin("*")
@RequestMapping(value = "/crossroad")
public class CrossroadController {

    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final ConnectionService connectionService;
    private final TrafficLightService trafficLightService;
    private final OptimizationService optimizationService;
    private final StartTimeService startTimeService;
    private final CarFlowService carFlowService;
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
            StartTimeService startTimeService,
            CarFlowService carFlowService,
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
        this.startTimeService = startTimeService;
        this.carFlowService = carFlowService;
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

        Crossroad crossroad = crossroadService.addCrossroad(
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


        for(Day day: Day.values()) {
            for(Hour hour : Hour.values()) {
                String startTimeId = startTimeService.getStartTimeIdByDayTime(day, hour);
                crossroad.getConnectionIds()
                        .forEach(
                                connIds -> connectionService.updateConnectionAddCarFlowId(
                                        connIds,
                                        carFlowService.addCarFlow(10, startTimeId, connIds).getId()
                                )
                        );
                crossroadService.getCrossroadById(crossroad.getId());
            }
        }

        return ResponseEntity
                .ok()
                .body(true);
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
}
