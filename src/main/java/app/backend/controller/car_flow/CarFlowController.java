package app.backend.controller.car_flow;

import app.backend.document.CarFlow;
import app.backend.service.CarFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/connections")
public class CarFlowController {

    @Autowired
    private CarFlowService carFlowService;

}
