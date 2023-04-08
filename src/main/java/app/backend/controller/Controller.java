package app.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//TODO dodanie metryk / testow czy cos

@RestController
public class Controller {

    @ResponseBody
    @GetMapping(value="/")
    public String home() {
        return "Hello world";
    }

}
