package app.backend.controller.optimization;

import org.springframework.web.bind.annotation.*;

@RestController
public class OptimizationController {

    @PostMapping(value="/optimizations")
    public String upload() {
        return "upload";
    }

    @GetMapping(value="/optimizations")
    public String list() {
        return "list";
    }

    @GetMapping(value="/optimizations/get/{id}/numeric")
    public String getNumeric(@PathVariable String id) {
        return "getNumeric";
    }

    @GetMapping(value="/optimizations/get/{id}/visual")
    public String getVisual(@PathVariable String id) {
        return "getVisual";
    }

}
