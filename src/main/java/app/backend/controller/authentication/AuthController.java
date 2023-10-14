package app.backend.controller.authentication;

import app.backend.document.User;
import app.backend.request.UserLogin;
import app.backend.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${secret.key}")
    private String secret;
    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(1);
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService){
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
            HttpHeaders headers = onAuthenticationSuccess(u);
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(true);
        }
    }

    public HttpHeaders onAuthenticationSuccess(User user) {
        String token = JWT.create() // 2
                .withSubject(user.getNickname()) // 3
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 4
                .sign(Algorithm.HMAC256(secret)); // 5
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers; // 6
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
