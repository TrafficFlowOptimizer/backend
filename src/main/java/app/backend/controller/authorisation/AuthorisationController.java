package app.backend.controller.authorisation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//is https needed???

@RestController
public class AuthorisationController {

    @PostMapping(value="/auth/login")
    public String login() {
        return "login";
    }

    @PostMapping(value="/auth/register")
    public String register() {
        return "register";
    }

}
