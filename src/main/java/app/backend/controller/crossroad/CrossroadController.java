package app.backend.controller.crossroad;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Thread.sleep;

@RestController
public class CrossroadController {
    @Autowired CrossroadService crossroadService;
    @Autowired CrossroadsUtils crossroadsUtils;

    @GetMapping(value="/crossroad")
    public List<Crossroad> getUserCrossroads(@RequestParam(required = false) String userId) {
        // for now if userId passed then returns PRIVATE for user and PUBLIC, else PUBLIC. In the future using session it will return PRIVATE for user and PUBLIC
        // maybe in the future option to get only privates or publics??
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
        crossroadService.addCrossroad(
                crossroad.getName(), crossroad.getLocation(), crossroad.getCreatorId(), crossroad.getType(),
                crossroad.getRoadIds(), crossroad.getCollisionIds(), crossroad.getConnectionIds(), crossroad.getTrafficLightIds()
        );
        return "ok";
    }

    @PutMapping(value="/crossroad")
    public String updateCrossroad(@RequestBody Crossroad crossroad) {
        try {
            crossroadService.updateCrossroad( crossroad.getId(),
                    crossroad.getName(), crossroad.getLocation(), crossroad.getCreatorId(), crossroad.getType(),
                    crossroad.getRoadIds(), crossroad.getCollisionIds(), crossroad.getConnectionIds(), crossroad.getTrafficLightIds()
            );
        } catch (Exception e) {e.printStackTrace();}
        return "ok";
    }

    @GetMapping(value="/crossroad/{crossroadId}/optimization/{videoId}/{time}",  produces = MediaType.APPLICATION_JSON_VALUE)
    public String getOptimization(@PathVariable String crossroadId, @PathVariable String videoId, @PathVariable int time) {
        String timeIntervalId = crossroadsUtils.analyseVideo(videoId);

        // TODO: lines below to be moved to utils??
        int serverPort = 9091;
        String result = "{}";
        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = crossroadsUtils.parseJSON(crossroadId, time);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(jsonData);

            InputStream optimizerResponse = socket.getInputStream();
            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";

            crossroadsUtils.addOptimizationResultsToDb(crossroadId, timeIntervalId, result);
            result = crossroadsUtils.parseOutput(result, crossroadId);

        } catch (Exception e) {
            try {
                sleep(time*1000L);
                result = Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {}
        }
        System.out.println(result);

        return result;
    }
}
