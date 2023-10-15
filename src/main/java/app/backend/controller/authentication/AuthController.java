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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @ResponseBody
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest)  {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String id = authentication.getName();
            User user = userService.getUserById(id);
            String token = jwtUtil.createToken(user);

            LoginResponse loginResponse = new LoginResponse(id, token);

            return ResponseEntity
                    .ok()
                    .body(loginResponse);

        } catch (BadCredentialsException e) {
            System.out.println(e.getMessage());
            return ResponseEntity
                    .status(UNAUTHORIZED)
                    .build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .build();
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