package app.backend.controller.intersection;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.CrossroadService;
import app.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/intersections")
public class IntersectionController {

    @Autowired
    private CrossroadService crossroadService;
    @Autowired
    private UserService userService;

    @PostMapping(value="")
    public String upload() {
        return "upload";
    }

    @GetMapping(value="/{user}")
    public List<Crossroad> list(@PathVariable String userId) { //trzeba podać userId; docelowo token będzie to załatwiał
        List<Crossroad> intersections = crossroadService.getCrossroadByCreatorId(userId);
        return intersections;
    }

    @GetMapping(value="/{id}")
    public Crossroad get(@PathVariable String id) {
        try {
            return crossroadService.getCrossroadById(id);
        } catch (Exception e) {
            return null;
        }
    }

    @PutMapping(value="/{id}")
    public String update() {
        return "update";
    }

}
