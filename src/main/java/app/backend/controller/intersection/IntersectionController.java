package app.backend.controller.intersection;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.CrossroadService;
import app.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class IntersectionController {

    @Autowired
    private CrossroadService crossroadService;
    @Autowired
    private UserService userService;

    @PostMapping(value="/intersections")
    public String upload() {
        return "upload";
    }

    @GetMapping(value="/intersections/{user}")
    public List<Crossroad> list(@PathVariable String userId) { //trzeba podać userId; docelowo token będzie to załatwiał
        List<Crossroad> intersections = crossroadService.getCrossroadByCreatorId(userId);
        return intersections;
    }

    @GetMapping(value="/intersections/{id}")
    public Crossroad get(@PathVariable String id) {
        try {
            return crossroadService.getCrossroadById(id);
        } catch (Exception e) {
            return null;
        }
    }

    @PutMapping(value="/intersections/{id}")
    public String update() {
        return "update";
    }

}
