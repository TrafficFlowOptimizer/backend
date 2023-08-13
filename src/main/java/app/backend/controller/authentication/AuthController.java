package app.backend.controller.authentication;

import app.backend.document.User;
import app.backend.request.UserLogin;
import app.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login")
    public String login(@RequestBody UserLogin user) {
        try {
            return String.valueOf(user.getPassword().equals(userService.getUserByNickname(user.getNickname()).getPassword()));
        } catch (Exception ignored) {
            return "false";
        }
    }

    @PostMapping(value="/register")
    public String register(@RequestBody @Valid User user) {
        userService.addUser(user.getFirstName(), user.getLastName(), user.getNickname(), user.getEmail(), user.getPassword());
        return "true";
    }

}
