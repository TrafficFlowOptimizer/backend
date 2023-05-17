package app.backend.service;

import app.backend.document.collision.Collision;
import app.backend.document.collision.CollisionType;
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
class CollisionServiceTest {

    @Autowired
    private CollisionService collisionService;

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
        collisionService.collisionRepository.deleteAll();
    }

    @Test
    public void getCollisionById_improperCollision_crossroadNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            collisionService.getCollisionById(id);
        });

        assertEquals(0, collisionService.collisionRepository.count());
        assertEquals("Cannot get collision with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getCollisionById_properCollision_correctCollision() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
        collisionService.addCollision("sddas", "dsaadsds", CollisionType.LIGHT);

        Collision found = null;
        try {
            found = collisionService.getCollisionById(collision.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, collisionService.collisionRepository.count());
        assertNotNull(found);
        assertEquals(trafficLight1Id, found.getTrafficLight1Id());
        assertEquals(trafficLight2Id, found.getTrafficLight2Id());
        assertEquals(type, found.getType());
    }

    @Test
    public void addCollision_properCollision_collisionAdded() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
        collisionService.addCollision("sddas", "dsaadsds", CollisionType.LIGHT);

        assertEquals(2, collisionService.collisionRepository.count());
        assertNotNull(collision);
        assertEquals(trafficLight1Id, collision.getTrafficLight1Id());
        assertEquals(trafficLight2Id, collision.getTrafficLight2Id());
        assertEquals(type, collision.getType());
    }

    @Test
    public void deleteCollisionById_properCollision_collisionDeleted() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);

        String id = collision.getId();
        try {
            collisionService.deleteCollisionById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            collisionService.getCollisionById(id);
        });

        assertEquals(0, collisionService.collisionRepository.count());
        assertEquals("Cannot get collision with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteCollisionById_improperCollision_collisionNotFound() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            collisionService.deleteCollisionById(id);
        });

        assertEquals(1, collisionService.collisionRepository.count());
        assertEquals("Cannot delete collision with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateCollision_properCollision_collisionUpdated() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);

        String id = collision.getId();
        String trafficLight1IdUpdated = "dsadasasd";
        String trafficLight2IdUpdated = "fdsfds";
        CollisionType typeUpdated = CollisionType.LIGHT;

        Collision updated = null;
        try {
            collisionService.updateCollision(id, trafficLight1IdUpdated, trafficLight2IdUpdated, typeUpdated);
            updated = collisionService.getCollisionById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, collisionService.collisionRepository.count());
        assertNotNull(updated);
        assertEquals(trafficLight1IdUpdated, updated.getTrafficLight1Id());
        assertEquals(trafficLight2IdUpdated, updated.getTrafficLight2Id());
        assertEquals(typeUpdated, updated.getType());
    }

    @Test
    public void updateCollision_improperCollision_collisionNotFound() {
        String trafficLight1Id = "abc";
        String trafficLight2Id = "ced";
        CollisionType type = CollisionType.HEAVY;

        Collision collision = collisionService.addCollision(trafficLight1Id, trafficLight2Id, type);

        String id = "";
        String trafficLight1IdUpdated = "dsadasasd";
        String trafficLight2IdUpdated = "fdsfds";
        CollisionType typeUpdated = CollisionType.LIGHT;

        Exception exception = assertThrows(Exception.class, () -> {
            collisionService.updateCollision(id, trafficLight1IdUpdated, trafficLight2IdUpdated, typeUpdated);
            collisionService.deleteCollisionById(id);
        });

        assertEquals(1, collisionService.collisionRepository.count());
        assertEquals("Cannot update collision with id: " + id + " because it does not exist.", exception.getMessage());
    }
}