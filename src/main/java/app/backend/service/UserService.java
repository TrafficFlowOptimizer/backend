package app.backend.service;

import app.backend.document.user.User;
import app.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

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

    public User getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElse(null);
    }

    public User addUser(String username, String email, String password) {
        try {
            return userRepository.insert(
                    new User(
                            username,
                            email,
                            password
                    )
            );
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }

    public User deleteUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        userRepository.deleteById(id);
        return user.get();
    }

    public User updateUser(String id, String username, String email, String password) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        User userToUpdate = user.get();
        userToUpdate.setUsername(username);
        userToUpdate.setEmail(email);
        userToUpdate.setPassword(password);

        userRepository.save(userToUpdate);

        return userToUpdate;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User with username " + username + "not found");
        }

        User user = optionalUser.get();
        List<String> roles = new ArrayList<>();
        roles.add("USER");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId())
                .password(user.getPassword())
                .roles(roles.toArray(new String[0]))
                .build();
    }
}
