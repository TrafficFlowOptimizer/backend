package app.backend.service;

import app.backend.document.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("test");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", ()-> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @DynamicPropertySource
    static void postgreSQLProperties(DynamicPropertyRegistry registry) {
        postgreSQLContainer.start();
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }

    @AfterEach
    public void cleanUpEach(){
        userService.userRepository.deleteAll();
    }

    @Test
    public void getUserById_improperUser_userNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserById(id);
        });

        assertEquals(0, userService.userRepository.count());
        assertEquals("Cannot get user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getUserById_properUser_correctUser() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, password);
        userService.addUser("Notjohn", "Notdoe", "stillnotJD", "password@123");

        User found = null;
        try {
            found = userService.getUserById(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, userService.userRepository.count());
        assertNotNull(found);
        assertEquals(firstName, found.getFirstName());
        assertEquals(lastName, found.getLastName());
        assertEquals(nickname, found.getNickname());
        assertEquals(password, found.getPassword());
    }

    @Test
    public void addUser_properUser_userAdded() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, password);

        assertEquals(1, userService.userRepository.count());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(nickname, user.getNickname());
        assertEquals(password, user.getPassword());
    }

    @Test
    public void deleteUserById_properUser_userDeleted() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, password);
        String id = user.getId();

        try {
            userService.deleteUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserById(id);
        });

        assertEquals(0, userService.userRepository.count());
        assertEquals("Cannot get user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteUserById_improperUser_userNotFound() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, password);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            userService.deleteUserById(id);
        });

        assertEquals(1, userService.userRepository.count());
        assertEquals("Cannot delete user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateUser_properUser_userUpdated() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, password);

        String id = user.getId();
        String firstNameUpdated = "Jon";
        String lastNameUpdated = "Tho";
        String nicknameUpdated = "JTho";
        String passwordUpdated = "password@234";

        User updated = null;
        try {
            userService.updateUser(id, firstNameUpdated, lastNameUpdated, nicknameUpdated, passwordUpdated);
            updated = userService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, userService.userRepository.count());
        assertNotNull(updated);
        assertEquals(firstNameUpdated, updated.getFirstName());
        assertEquals(lastNameUpdated, updated.getLastName());
        assertEquals(nicknameUpdated, updated.getNickname());
        assertEquals(passwordUpdated, updated.getPassword());
    }

    @Test
    public void updateUser_improperUser_userNotFound() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String password = "password@123";

        userService.addUser(firstName, lastName, nickname, password);

        String id = "";
        String firstNameUpdated = "Jon";
        String lastNameUpdated = "Tho";
        String nicknameUpdated = "JTho";
        String passwordUpdated = "password@234";

        Exception exception = assertThrows(Exception.class, () -> {
            userService.updateUser(id, firstNameUpdated, lastNameUpdated, nicknameUpdated, passwordUpdated);
            userService.deleteUserById(id);
        });

        assertEquals(1, userService.userRepository.count());
        assertEquals("Cannot update user with id: " + id + " because it does not exist.", exception.getMessage());
    }
}