package app.backend.controller.intersection;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.CrossroadService;
import app.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/intersections")
public class IntersectionController {

    private final CrossroadService crossroadService;

    @Autowired
    public IntersectionController(CrossroadService crossroadService, UserService userService) {
        this.crossroadService = crossroadService;
    }

    @PostMapping(value = "")
    public String upload() {
        return "upload";
    }

    @GetMapping(value = "/{user}")
    public ResponseEntity<List<Crossroad>> list(@PathVariable String userId) { //trzeba podać userId; docelowo token będzie to załatwiał
        List<Crossroad> intersections = crossroadService.getCrossroadByCreatorId(userId);
        return ResponseEntity
                .ok()
                .body(intersections);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Crossroad> get(@PathVariable String id) {
        Crossroad crossroad = crossroadService.getCrossroadById(id);
        if (crossroad != null) {
            return ResponseEntity
                    .ok()
                    .body(crossroad);
        } else {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
    }
}
