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

    private final UserService userService;

    @Autowired
    public UserServiceTest(UserService userService) {
        this.userService = userService;
    }

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
        userService.getUserRepository().deleteAll();
    }

    @Test
    public void getUserById_improperUser_userNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserById(id);
        });

        assertEquals(0, userService.getUserRepository().count());
        assertEquals("Cannot get user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getUserById_properUser_correctUser() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, email, password);
        userService.addUser("Notjohn", "Notdoe", "stillnotJD", "email@email.pl", "password@123");

        User found = null;
        try {
            found = userService.getUserById(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, userService.getUserRepository().count());
        assertNotNull(found);
        assertEquals(firstName, found.getFirstName());
        assertEquals(lastName, found.getLastName());
        assertEquals(nickname, found.getNickname());
        assertEquals(email, found.getEmail());
        assertEquals(password, found.getPassword());
    }

    @Test
    public void addUser_properUser_userAdded() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, email, password);

        assertEquals(1, userService.getUserRepository().count());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(nickname, user.getNickname());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
    }

    @Test
    public void deleteUserById_properUser_userDeleted() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, email, password);
        String id = user.getId();

        try {
            userService.deleteUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserById(id);
        });

        assertEquals(0, userService.getUserRepository().count());
        assertEquals("Cannot get user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteUserById_improperUser_userNotFound() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, email, password);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            userService.deleteUserById(id);
        });

        assertEquals(1, userService.getUserRepository().count());
        assertEquals("Cannot delete user with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateUser_properUser_userUpdated() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        User user = userService.addUser(firstName, lastName, nickname, email, password);

        String id = user.getId();
        String firstNameUpdated = "Jon";
        String lastNameUpdated = "Tho";
        String nicknameUpdated = "JTho";
        String emailUpdated = "j.dup@gmail.com";
        String passwordUpdated = "password@234";

        User updated = null;
        try {
            userService.updateUser(id, firstNameUpdated, lastNameUpdated, nicknameUpdated, emailUpdated, passwordUpdated);
            updated = userService.getUserById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, userService.getUserRepository().count());
        assertNotNull(updated);
        assertEquals(firstNameUpdated, updated.getFirstName());
        assertEquals(lastNameUpdated, updated.getLastName());
        assertEquals(nicknameUpdated, updated.getNickname());
        assertEquals(emailUpdated, updated.getEmail());
        assertEquals(passwordUpdated, updated.getPassword());
    }

    @Test
    public void updateUser_improperUser_userNotFound() {
        String firstName = "John";
        String lastName = "Doe";
        String nickname = "JDoe";
        String email = "j.d@gmail.com";
        String password = "password@123";

        userService.addUser(firstName, lastName, nickname, email, password);

        String id = "";
        String firstNameUpdated = "Jon";
        String lastNameUpdated = "Tho";
        String nicknameUpdated = "JTho";
        String emailUpdated = "j.dup@gmail.com";
        String passwordUpdated = "password@234";

        Exception exception = assertThrows(Exception.class, () -> {
            userService.updateUser(id, firstNameUpdated, lastNameUpdated, nicknameUpdated, emailUpdated, passwordUpdated);
            userService.deleteUserById(id);
        });

        assertEquals(1, userService.getUserRepository().count());
        assertEquals("Cannot update user with id: " + id + " because it does not exist.", exception.getMessage());
    }
}