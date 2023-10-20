package app.backend.controller.optimization;

import app.backend.document.Optimization;
import app.backend.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/optimization")
public class OptimizationController {
    private final OptimizationService optimizationService;

    @Autowired
    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @GetMapping(value = "/{optimizationId}")
    public ResponseEntity<Optimization> getOptimization(@PathVariable String optimizationId) {
        Optimization optimization = optimizationService.getOptimizationById(optimizationId);

        if (optimization != null) {
            return ResponseEntity
                    .ok()
                    .body(optimization);
        } else {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
    }

    @GetMapping(value = "/{crossroadId}")
    public ResponseEntity<List<Optimization>> list(@PathVariable String crossroadId) {
        Iterable<Optimization> optimizations = optimizationService.getOptimizationsByCrossroadId(crossroadId);

        List<Optimization> ret = new LinkedList<>();
        for (Optimization optimization : optimizations) {
            ret.add(optimization);
        }

        return ResponseEntity
                .ok()
                .body(ret);
    }
}
