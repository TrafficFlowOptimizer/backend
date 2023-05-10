package app.backend.service;

import app.backend.document.User;
import app.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public User getUserById(String id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()){
            throw new Exception("Cannot get user with id: " + id + " because it does not exist.");
        }

        return user.get();
    }

    public User addUser(String firstName, String lastName, String nickname, String password){
        return userRepository.insert(new User(firstName, lastName, nickname, password));
    }

    public User deleteUserById(String id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new Exception("Cannot delete user with id: " + id + " because it does not exist.");
        }
        userRepository.deleteById(id);
        return user.get();
    }

    public User updateUser(String id, String firstName, String lastName, String nickname, String password) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()){throw new Exception("Cannot update user with id: " + id + " because it does not exist.");}
        User userToUpdate = user.get();

        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setNickname(nickname);
        userToUpdate.setPassword(password);

        userRepository.save(userToUpdate);

        return userToUpdate;
    }
}
