package app.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @ResponseBody
    @GetMapping(value="/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello from secured endpoint");
    }

}
