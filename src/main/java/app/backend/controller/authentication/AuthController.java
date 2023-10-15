package app.backend.controller.authentication;

import app.backend.authentication.JwtUtil;
import app.backend.document.User;
import app.backend.request.auth.LoginReq;
import app.backend.response.auth.ErrorRes;
import app.backend.response.auth.LoginRes;
import app.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @ResponseBody
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody LoginReq loginReq)  {

        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReq.getUsername(), loginReq.getPassword()));
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            String token = jwtUtil.createToken(user);
            LoginRes loginRes = new LoginRes(email,token);

            return ResponseEntity.ok(loginRes);

        }catch (BadCredentialsException e){
            ErrorRes errorResponse = new ErrorRes(HttpStatus.BAD_REQUEST,"Invalid username or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch (Exception e){
            ErrorRes errorResponse = new ErrorRes(HttpStatus.BAD_REQUEST, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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