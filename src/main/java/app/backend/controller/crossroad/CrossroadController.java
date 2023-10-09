package app.backend.controller.crossroad;

import app.backend.document.crossroad.Crossroad;
import app.backend.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Thread.sleep;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(value = "/crossroad")
public class CrossroadController {

    private final CrossroadService crossroadService;
    private final OptimizationService optimizationService;
    private final CrossroadsUtils crossroadsUtils;

    @Autowired
    public CrossroadController(
            CrossroadService crossroadService,
            OptimizationService optimizationService,
            CrossroadsUtils crossroadsUtils
    ) {
        this.crossroadService = crossroadService;
        this.optimizationService = optimizationService;
        this.crossroadsUtils = crossroadsUtils;
    }

    @GetMapping(value = "")
    public ResponseEntity<List<Crossroad>> getUserCrossroads(@RequestParam(required = false) String userId) {
        // for now if userId passed then returns PRIVATE for user and PUBLIC, else PUBLIC. In the future using session it will return PRIVATE for user and PUBLIC
        // maybe in the future option to get only privates or publics??
        if(userId != null) {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getCrossroadsByCreatorIdOrPublic(userId));
        }
        else {
            return ResponseEntity
                    .ok()
                    .body(crossroadService.getPublicCrossroads());
        }
    }

    @GetMapping(value="/{crossroadId}")
    public ResponseEntity<Crossroad> getCrossroad(@PathVariable String crossroadId) {
        Crossroad crossroad = crossroadService.getCrossroadById(crossroadId);
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

    @PostMapping()
    public ResponseEntity<Boolean> addCrossroad(@RequestBody Crossroad crossroad) {
        crossroadService.addCrossroad(
                crossroad.getName(),
                crossroad.getLocation(),
                crossroad.getCreatorId(),
                crossroad.getType(),
                crossroad.getRoadIds(),
                crossroad.getCollisionIds(),
                crossroad.getConnectionIds(),
                crossroad.getTrafficLightIds()
        );
        return ResponseEntity
                .ok()
                .body(true); // TODO: czy użytkownik może dodać 2 skrzyżowania o tej samej nazwie?
    }

    @PutMapping()
    public ResponseEntity<Crossroad> updateCrossroad(@RequestBody Crossroad crossroad) {
        Crossroad updatedCrossroad = crossroadService.updateCrossroad(
                crossroad.getId(),
                crossroad.getName(),
                crossroad.getLocation(),
                crossroad.getCreatorId(),
                crossroad.getType(),
                crossroad.getRoadIds(),
                crossroad.getCollisionIds(),
                crossroad.getConnectionIds(),
                crossroad.getTrafficLightIds()
        );

        if (updatedCrossroad != null) {
            return ResponseEntity
                    .ok()
                    .body(updatedCrossroad);
        } else {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
    }

    @GetMapping(value="/{crossroadId}/optimization/{videoId}/{time}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOptimization( // TODO: change to JSONObject
            @PathVariable String crossroadId,
            @PathVariable String videoId,
            @PathVariable int time
    ) {
        int serverPort = 9091; // TODO: get from variable from environment
        String result = "{}";
        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = crossroadsUtils.parseJSON(crossroadId, time);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(jsonData);

            InputStream optimizerResponse = socket.getInputStream();
            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";

            String timeIntervalId = crossroadsUtils.getTimeIntervalId(videoId);
            if (timeIntervalId == null) {
                return ResponseEntity
                        .status(NOT_FOUND)
                        .build();
            }
            crossroadsUtils.addOptimizationResultsToDb(crossroadId, timeIntervalId, result);
            throw new RuntimeException(); //TODO: for now
//            result = crossroadsUtils.parseOutput(result, crossroadId);

        } catch (Exception e) {
            try {
                sleep(time*1000L);
                result = Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {}
        }
        System.out.println(result);

        return ResponseEntity
                .ok()
                .body(result);
    }

    private List<List<Integer>> convertJSONArrayToArray(String jsonArray){
        JSONObject obj = new JSONObject(jsonArray);
        JSONArray arr = obj.getJSONArray("results");
        List<List<Integer>> ar = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            List<Integer> row = new ArrayList<>();
            for(int j=0;j<arr.getJSONArray(0).length();j++){
                row.add((int) arr.getJSONArray(i).get(j));
            }
            ar.add(row);
        }
        return ar;
    }

    @GetMapping(value="/{crossroadId}/optimization/{time}",  produces = MediaType.APPLICATION_JSON_VALUE) // TODO
    public String getOptimizationWithoutVideo(@PathVariable String crossroadId, @PathVariable int time) {
        int serverPort = 9091;
        String result = "{results: \"ERROR\"}";
        try (Socket socket = new Socket("localhost", serverPort)) {
            JSONObject jsonData = crossroadsUtils.parseJSON(crossroadId, time);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(jsonData);

            InputStream optimizerResponse = socket.getInputStream();
            Scanner s = new Scanner(optimizerResponse).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";
            List<List<Integer>> resultArray = convertJSONArrayToArray(result);
            //TODO: timeIntervalID powinno być jako argument

            optimizationService.addOptimization(crossroadId, "0", resultArray);

            result = getOptimizationResults(crossroadId, "0");

        } catch (Exception e) {
            try {
                sleep(time*1000L);
                result = Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {}
        }
        System.out.println(result);

        return result;
    }

    @GetMapping(value="/{crossroadId}/optimization_results",  produces = MediaType.APPLICATION_JSON_VALUE) // TODO
    public String getOptimizationResults( @PathVariable String crossroadId, String timeIntervalID){
        try {
            List<List<Integer>> newestResult = optimizationService.getNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            List<List<Integer>> secondNewestResult = optimizationService.getSecondNewestOptimizationByCrossroadId(crossroadId, timeIntervalID).getResults();
            return crossroadsUtils.parseOutput(newestResult, secondNewestResult, crossroadId);
        } catch (Exception e) {
            try {
                return Files.readString(Paths.get("newTemplateOutput.json"));
            } catch (Exception ignored) {
                throw new RuntimeException("Error with reading template output.");
            }
        }
    }
}
