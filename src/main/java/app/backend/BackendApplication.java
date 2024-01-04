package app.backend;

import app.backend.document.user.Role;
import app.backend.document.user.User;
import app.backend.repository.UserRepository;
import nu.pattern.OpenCV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        OpenCV.loadLocally();
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        List<User> admins = users.stream().filter(u -> {return u.getRole().equals(Role.ADMIN);}).toList();
        if (admins.size() == 0){
            userRepository.insert(new User("admin", "admin@gmail.com", passwordEncoder.encode("12345678"), Role.ADMIN));
        }
    }
}
