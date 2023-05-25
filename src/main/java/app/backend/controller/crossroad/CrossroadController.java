package app.backend.controller.crossroad;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.CrossroadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CrossroadController {

    @Autowired
    CrossroadService crossroadService;

    @GetMapping(value="/crossroad")
    public List<Crossroad> getUserCrossroads(@RequestParam(required = false) String userId) { // for now if userId passed then returns PRIVATE for user and PUBLIC, else PUBLIC. In the future using session it will return PRIVATE for user and PUBLIC
        if(userId != null) {
            return crossroadService.getCrossroadsByCreatorIdOrPublic(userId);
        }
        else {
            return crossroadService.getCrossroadsPublic();
        }
    }

    @GetMapping(value="/crossroad/{crossroadId}")
    public Crossroad getCrossroad(@PathVariable String crossroadId) {
        Crossroad crossroad = null;
        try {
            crossroad = crossroadService.getCrossroadById(crossroadId);
        } catch (Exception e) {e.printStackTrace();}
        return crossroad;
    }

    @PostMapping(value="/crossroad")
    public String addCrossroad(@RequestBody Crossroad crossroad) {
        return "/crossroad/{crossroadId}";
    }

    @PutMapping(value="/crossroad/{crossroadId}")
    public String updateCrossroad(@PathVariable String crossroadId) {
        return "/crossroad/{crossroadId}";
    }
}
