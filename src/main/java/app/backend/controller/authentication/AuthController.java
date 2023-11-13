package app.backend.controller.authentication;

import app.backend.authentication.JwtUtil;
import app.backend.document.User;
import app.backend.request.auth.LoginRequest;
import app.backend.response.auth.LoginResponse;
import app.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public AuthController(
            BCryptPasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(UNAUTHORIZED)
                    .build();
        }

        String id = authentication.getName();
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
        String token = jwtUtil.createToken(user);

        return ResponseEntity
                .ok()
                .body(new LoginResponse(id, token));
    }

    @PostMapping(value = "/register")
    public ResponseEntity<Boolean> register(@RequestBody @Valid User user) {
        if (userService.addUser(
                user.getUsername(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword())
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
