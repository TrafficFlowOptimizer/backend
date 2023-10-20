package app.backend.controller.user;

import app.backend.document.User;
import app.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "{nickname}")
    public ResponseEntity<String> userIdByNickname(@RequestParam String nickname) {
        User user = userService.getUserByNickname(nickname);
        if (user != null) {
            return ResponseEntity
                    .ok()
                    .body(user.getId());
        } else {
            return ResponseEntity
                    .status(NOT_FOUND)
                    .build();
        }
    }
}
