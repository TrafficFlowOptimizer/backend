package app.backend.controller.connection;

import app.backend.document.CarFlow;
import app.backend.service.CarFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/connection")
public class ConnectionController {

    @Autowired
    private CarFlowService carFlowService;

    @GetMapping(value="/get/{id}")
    public CarFlow getCarFlow(
            @PathVariable("id") String id
    ) throws Exception {
        return carFlowService.getCarFlowById(id);
    }

    @PutMapping(value="/update/{id}")
    public CarFlow updateCarFlow(
            @PathVariable("id") String id,
            @RequestParam("carFlowPm") int carFlowPm,
            @RequestParam("startTime") LocalTime startTime,
            @RequestParam("endTime") LocalTime endTime
    ) throws Exception {
        return carFlowService.updateCarFlow(id, carFlowPm, startTime, endTime);
    }

    @PostMapping(value="/add")
    public CarFlow addCarFlow(
            @RequestParam("carFlowPm") int carFlowPm,
            @RequestParam("startTime") LocalTime startTime,
            @RequestParam("endTime") LocalTime endTime
    ) {
        return carFlowService.addCarFlow(carFlowPm, startTime, endTime);
    }

    @DeleteMapping(value="/delete/{id}")
    public CarFlow deleteCarFlow(
            @PathVariable("id") String id
    ) throws Exception {
        System.out.println(id);
        return carFlowService.deleteCarFlowById(id);
    }

}
