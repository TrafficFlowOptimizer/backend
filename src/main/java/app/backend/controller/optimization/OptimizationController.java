package app.backend.controller.optimization;

import app.backend.document.Optimization;
import app.backend.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
public class OptimizationController {
    @Autowired
    OptimizationService optimizationService;

    @GetMapping(value="/optimization/{optimizationId}")
    public Optimization getOptimization(@PathVariable String optimizationId) {
        try {
            return optimizationService.getOptimizationById(optimizationId);
        } catch (Exception e) {throw new RuntimeException(e);}
    }

//    @PostMapping(value="/optimization")
//    public void upload(@RequestBody Optimization optimization) {
//        optimizationService.addOptimization(optimization.getCrossroadId(), optimization.getVersion(), optimization.getSequences());
//    }

    @GetMapping(value="/optimizations/{crossroadId}")
    public List<Optimization> list(@PathVariable String crossroadId) {
        Iterable<Optimization> optimizations =  optimizationService.getOptimizationsByCrossroadId(crossroadId);
        List<Optimization> ret = new LinkedList<>();
        for(Optimization optimization : optimizations) {
            ret.add(optimization);
        }
        return ret;
    }

//    @GetMapping(value="/optimization/get/{id}/numeric")
//    public String getNumeric(@PathVariable String id) {
//        return "getNumeric";
//    }
//
//    @GetMapping(value="/optimization/get/{id}/visual")
//    public String getVisual(@PathVariable String id) {
//        return "getVisual";
//    }
}
