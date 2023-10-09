package app.backend.service;

import app.backend.document.User;
import app.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String id) {
        return userRepository
                .findById(id)
                .orElse(null);
    }

    public User getUserByNickname(String nickname) {
        return userRepository
                .findByNickname(nickname)
                .orElse(null);
    }

    public User addUser(String firstName, String lastName, String nickname, String email, String password) {
        return userRepository.insert(
                new User(
                        firstName,
                        lastName,
                        nickname,
                        email,
                        password
                )
        );
    }

    public User deleteUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        userRepository.deleteById(id);
        return user.get();
    }

    public User updateUser(String id, String firstName, String lastName, String nickname, String email, String password) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        User userToUpdate = user.get();
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setNickname(nickname);
        userToUpdate.setEmail(email);
        userToUpdate.setPassword(password);

        userRepository.save(userToUpdate);

        return userToUpdate;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
