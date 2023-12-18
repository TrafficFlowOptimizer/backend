package app.backend.controller.optimization;

import app.backend.document.time.Day;
import app.backend.document.time.Hour;
import app.backend.response.optimization.OptimizationResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/optimization")
public class OptimizationController {

    private final OptimizationUtils optimizationUtils;

    @Autowired
    public OptimizationController(OptimizationUtils optimizationUtils) {
        this.optimizationUtils = optimizationUtils;
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
        return optimizationUtils.orderOptimization(crossroadId, optimizationTime, day, hour);
    }


    @GetMapping(value = "/result/{crossroadId}")
    public ResponseEntity<OptimizationResultResponse> getOptimizationResult(
            @PathVariable String crossroadId,
            @RequestParam Day day,
            @RequestParam Hour hour
    ) {
        return optimizationUtils.retrieveOptimizationResult(crossroadId, day, hour);
    }

    @PostMapping(value = "/base")
    public ResponseEntity<Void> addTrafficLightsCycles(
            @RequestParam("file") MultipartFile trafficLightsCycles,
            @RequestParam("crossroadId") String crossroadId,
            @RequestParam(value = "day", required = false) Day day,
            @RequestParam(value = "hour", required = false) Hour hour
    ) {
        if (day == null || hour == null) {
            return optimizationUtils.addTrafficLightsCycles(trafficLightsCycles, crossroadId);
        } else {
            return optimizationUtils.addTrafficLightsCycles(trafficLightsCycles, crossroadId, day, hour);
        }
    }
}
