package app.backend.controller.crossroad;

import app.backend.document.Video;
import app.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CrossroadsUtils {
    private final CrossroadService crossroadService;
    private final RoadService roadService;
    private final CollisionService collisionService;
    private final TrafficLightService trafficLightService;
    private final ConnectionService connectionService;
    private final CarFlowService carFlowService;
    private final OptimizationService optimizationService;
    private final VideoService videoService;

    @Autowired
    public CrossroadsUtils(
            CrossroadService crossroadService,
            RoadService roadService,
            CollisionService collisionService,
            TrafficLightService trafficLightService,
            ConnectionService connectionService,
            CarFlowService carFlowService,
            OptimizationService optimizationService,
            VideoService videoService
    ) {
        this.crossroadService = crossroadService;
        this.roadService = roadService;
        this.collisionService = collisionService;
        this.trafficLightService = trafficLightService;
        this.connectionService = connectionService;
        this.carFlowService = carFlowService;
        this.optimizationService = optimizationService;
        this.videoService = videoService;
    }

    public String getStartTimeId(String videoId) {
        Video video = videoService.getVideo(videoId);
        if (video == null) {
            return null;
        }

        return video.getStartTimeId();
    }

}
