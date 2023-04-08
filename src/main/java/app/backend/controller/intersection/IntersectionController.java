package app.backend.controller.intersection;

import org.springframework.web.bind.annotation.*;

@RestController
public class IntersectionController {

    @PostMapping(value="/intersections/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping(value="/intersections")
    public String list() {
        return "list";
    }

    @GetMapping(value="/intersections/{id}")
    public String get(@PathVariable String id) {
        return "get";
    }

}
