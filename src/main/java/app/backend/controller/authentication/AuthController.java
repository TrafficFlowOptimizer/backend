package app.backend.controller.authentication;

import app.backend.document.User;
import app.backend.request.UserLogin;
import app.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService  userService){
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Boolean> login(@RequestBody UserLogin user) {
        User u = userService.getUserByNickname(user.getNickname());
        if (u == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .body(false);
        } else if (!u.getPassword().equals(user.getPassword())) {
            return ResponseEntity
                    .status(UNAUTHORIZED)
                    .body(false);
        } else {
            return ResponseEntity
                    .ok()
                    .body(true);
        }
    }

    @PostMapping(value="/register")
    public ResponseEntity<Boolean> register(@RequestBody @Valid User user) {
        if (userService.addUser(
                user.getFirstName(),
                user.getLastName(),
                user.getNickname(),
                user.getEmail(),
                user.getPassword()
        ) != null) {
            return ResponseEntity
                    .ok()
                    .body(true);
        } else {
            return ResponseEntity
                    .status(UNPROCESSABLE_ENTITY)
                    .body(false);
        }
    }
}
