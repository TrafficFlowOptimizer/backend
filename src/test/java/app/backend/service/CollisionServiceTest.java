package app.backend.service;

import app.backend.document.Collision;
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

    private final CollisionService collisionService;

    @Autowired
    public CollisionServiceTest(CollisionService collisionService) {
        this.collisionService = collisionService;
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
        collisionService.getCollisionRepository().deleteAll();
    }

    @Test
    public void getCollisionById_improperCollision_crossroadNotFound() {
        String id = "";
        assertNull(collisionService.getCollisionById(id));
        assertEquals(0, collisionService.getCollisionRepository().count());
    }

    @Test
    public void getCollisionById_properCollision_correctCollision() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        Collision collision = collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);
        collisionService.addCollision(1, "nm", "sddas", "dsaadsds", true);

        Collision found = collisionService.getCollisionById(collision.getId());

        assertEquals(2, collisionService.getCollisionRepository().count());
        assertNotNull(found);
        assertEquals(index, found.getIndex());
        assertEquals(name, found.getName());
        assertEquals(connection1Id, found.getConnection1Id());
        assertEquals(connection2Id, found.getConnection2Id());
        assertEquals(bothCanBeOn, found.getBothCanBeOn());
    }

    @Test
    public void addCollision_properCollision_collisionAdded() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        Collision collision = collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);
        collisionService.addCollision(1, "nm", "sddas", "dsaadsds", true);

        assertEquals(2, collisionService.getCollisionRepository().count());
        assertNotNull(collision);
        assertEquals(index, collision.getIndex());
        assertEquals(name, collision.getName());
        assertEquals(connection1Id, collision.getConnection1Id());
        assertEquals(connection2Id, collision.getConnection2Id());
        assertEquals(bothCanBeOn, collision.getBothCanBeOn());
    }

    @Test
    public void deleteCollisionById_properCollision_collisionDeleted() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        Collision collision = collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);

        String id = collision.getId();
        collisionService.deleteCollisionById(id);

        assertNull(collisionService.getCollisionById(id));
        assertEquals(0, collisionService.getCollisionRepository().count());
    }

    @Test
    public void deleteCollisionById_improperCollision_collisionNotFound() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);
        String id = "";

        assertNull(collisionService.deleteCollisionById(id));
        assertEquals(1, collisionService.getCollisionRepository().count());
    }

    @Test
    public void updateCollision_properCollision_collisionUpdated() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        Collision collision = collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);

        String id = collision.getId();
        int indexUpdated = 1;
        String nameUpdated = "updt";
        String connection1IdUpdated = "dsadasasd";
        String connection2IdUpdated = "fdsfds";
        boolean bothCanBeOnUpdated = true;

        collisionService.updateCollision(id, indexUpdated, nameUpdated, connection1IdUpdated, connection2IdUpdated, bothCanBeOnUpdated);
        Collision updated = collisionService.getCollisionById(id);

        assertEquals(1, collisionService.getCollisionRepository().count());
        assertNotNull(updated);
        assertEquals(indexUpdated, updated.getIndex());
        assertEquals(nameUpdated, updated.getName());
        assertEquals(connection1IdUpdated, updated.getConnection1Id());
        assertEquals(connection2IdUpdated, updated.getConnection2Id());
        assertEquals(bothCanBeOnUpdated, updated.getBothCanBeOn());
    }

    @Test
    public void updateCollision_improperCollision_collisionNotFound() {
        int index = 0;
        String name = "name";
        String connection1Id = "abc";
        String connection2Id = "ced";
        boolean bothCanBeOn = false;

        collisionService.addCollision(index, name, connection1Id, connection2Id, bothCanBeOn);

        String id = "";
        int indexUpdated = 1;
        String nameUpdated = "updt";
        String connection1IdUpdated = "dsadasasd";
        String connection2IdUpdated = "fdsfds";
        boolean bothCanBeOnUpdated = true;

        assertNull(collisionService.updateCollision(id, indexUpdated, nameUpdated, connection1IdUpdated, connection2IdUpdated, bothCanBeOnUpdated));
        assertNull(collisionService.deleteCollisionById(id));
        assertEquals(1, collisionService.getCollisionRepository().count());
    }
}