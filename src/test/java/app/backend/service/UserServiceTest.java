package app.backend.service;

import app.backend.document.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);
    private final UserService userService;

    @Autowired
    public UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        userService.getUserRepository().deleteAll();
    }

    @Test
    public void getUserById_improperUser_userNotFound() {
        String id = "";
        assertNull(userService.getUserById(id));
        assertEquals(0, userService.getUserRepository().count());
    }

    @Test
    public void getUserById_properUser_correctUser() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(username, email, password);
        userService.addUser("stillnotJD", "email@email.pl", "password@123");

        User found = userService.getUserById(user.getId());

        assertEquals(2, userService.getUserRepository().count());
        assertNotNull(found);
        assertEquals(username, found.getUsername());
        assertEquals(email, found.getEmail());
        assertEquals(password, found.getPassword());
    }

    @Test
    public void addUser_properUser_userAdded() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(username, email, password);

        assertEquals(1, userService.getUserRepository().count());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
    }

    @Test
    public void deleteUserById_properUser_userDeleted() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(username, email, password);
        String id = user.getId();

        userService.deleteUserById(id);

        assertNull(userService.getUserById(id));
        assertEquals(0, userService.getUserRepository().count());
    }

    @Test
    public void deleteUserById_improperUser_userNotFound() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(username, email, password);
        String id = "";

        assertNull(userService.deleteUserById(id));
        assertNotNull(userService.getUserById(user.getId()));
        assertEquals(1, userService.getUserRepository().count());
    }

    @Test
    public void updateUser_properUser_userUpdated() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(username, email, password);

        String id = user.getId();
        String firstNameUpdated = "Jon";
        String lastNameUpdated = "Tho";
        String usernameUpdated = "JTho";
        String emailUpdated = "j.dup@gmail.com";
        String passwordUpdated = "password@234";

        try {
            userService.updateUser(id, usernameUpdated, emailUpdated, passwordUpdated);
        } catch (Exception e) {
            e.printStackTrace();
        }

        User updated = userService.getUserById(id);

        assertEquals(1, userService.getUserRepository().count());
        assertNotNull(updated);
        assertEquals(usernameUpdated, updated.getUsername());
        assertEquals(emailUpdated, updated.getEmail());
        assertEquals(passwordUpdated, updated.getPassword());
    }

    @Test
    public void updateUser_improperUser_userNotFound() {
        String username = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        userService.addUser(username, email, password);

        String id = "";
        String usernameUpdated = "JTho";
        String emailUpdated = "j.dup@gmail.com";
        String passwordUpdated = "password@234";

        assertNull(userService.updateUser(id, usernameUpdated, emailUpdated, passwordUpdated));

        userService.deleteUserById(id);
        assertEquals(1, userService.getUserRepository().count());
    }
}