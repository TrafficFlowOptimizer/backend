package app.backend.controller.optimization;

import app.backend.document.Optimization;
import app.backend.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/optimization")
public class OptimizationController {
    private final OptimizationService optimizationService;

    @Autowired
    public OptimizationController(OptimizationService optimizationService){
        this.optimizationService = optimizationService;
    }

    @GetMapping(value="/{optimizationId}")
    public Optimization getOptimization(@PathVariable String optimizationId) {
        try {
            return optimizationService.getOptimizationById(optimizationId);
        } catch (Exception e) {throw new RuntimeException(e);}
    }

    @GetMapping(value="/{crossroadId}")
    public List<Optimization> list(@PathVariable String crossroadId) {
        Iterable<Optimization> optimizations =  optimizationService.getOptimizationsByCrossroadId(crossroadId);
        List<Optimization> ret = new LinkedList<>();
        for(Optimization optimization : optimizations) {
            ret.add(optimization);
        }
        return ret;
    }
}
