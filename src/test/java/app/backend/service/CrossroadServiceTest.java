package app.backend.service;

import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrossroadServiceTest {

    @Autowired
    private CrossroadService crossroadService;

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
        crossroadService.crossroadRepository.deleteAll();
    }

    @Test
    public void getCrossroadById_improperCrossroad_crossroadNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            crossroadService.getCrossroadById(id);
        });

        assertEquals(0, crossroadService.crossroadRepository.count());
        assertEquals("Cannot get crossroad with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getCrossroadById_properCrossroad_correctRoad() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);
        crossroadService.addCrossroad("Notjohn", "Notdoe", "sdf", CrossroadType.PUBLIC, new ArrayList<>(), new ArrayList<>());

        Crossroad found = null;
        try {
            found = crossroadService.getCrossroadById(crossroad.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, crossroadService.crossroadRepository.count());
        assertNotNull(found);
        assertEquals(name, found.getName());
        assertEquals(location, found.getLocation());
//        assertEquals(ownerId, found.getOwnerId()); TODO
        assertEquals(type, found.getType());
        assertEquals(roadIDs, found.getRoadIds());
        assertEquals(collisionIDs, found.getCollisionIds());
    }

    @Test
    public void addCrossroad_properCrossroad_roadAdded() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);

        assertEquals(1, crossroadService.crossroadRepository.count());
        assertEquals(name, crossroad.getName());
        assertEquals(location, crossroad.getLocation());
//        assertEquals(ownerId, crossroad.getOwnerId()); TODO
        assertEquals(type, crossroad.getType());
        assertEquals(roadIDs, crossroad.getRoadIds());
        assertEquals(collisionIDs, crossroad.getCollisionIds());
    }

    @Test
    public void deleteCrossroadById_properCrossroad_roadDeleted() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);

        String id = crossroad.getId();
        try {
            crossroadService.deleteCrossroadById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            crossroadService.getCrossroadById(id);
        });

        assertEquals(0, crossroadService.crossroadRepository.count());
        assertEquals("Cannot get crossroad with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteCrossroadById_improperCrossroad_crossroadNotFound() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            crossroadService.deleteCrossroadById(id);
        });

        assertEquals(1, crossroadService.crossroadRepository.count());
        assertEquals("Cannot delete crossroad with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateCrossroad_properCrossroad_roadUpdated() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);

        String id = crossroad.getId();
        String nameUpdated = "Johna";
        String locationUpdated = "Doea";
        String ownerIdUpdated = "abca";
        CrossroadType typeUpdated = CrossroadType.PRIVATE;
        List<String> roadIDsUpdated = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        roadIDs.add("234");
        List<String> collisionIDsUpdated = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        collisionIDs.add("gfd");

        Crossroad updated = null;
        try {
            crossroadService.updateCrossroad(id, nameUpdated, locationUpdated, ownerIdUpdated, typeUpdated, roadIDsUpdated, collisionIDsUpdated);
            updated = crossroadService.getCrossroadById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, crossroadService.crossroadRepository.count());
        assertNotNull(updated);
        assertEquals(nameUpdated, updated.getName());
        assertEquals(locationUpdated, updated.getLocation());
//        assertEquals(ownerIdUpdated, updated.getOwnerId()); TODO
        assertEquals(typeUpdated, updated.getType());
        assertEquals(roadIDsUpdated, updated.getRoadIds());
        assertEquals(collisionIDsUpdated, updated.getCollisionIds());
    }

    @Test
    public void updateCrossroad_improperCrossroad_crossroadNotFound() {
        String name = "John";
        String location = "Doe";
        String ownerId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs);

        String id = "";
        String nameUpdated = "Johna";
        String locationUpdated = "Doea";
        String ownerIdUpdated = "abca";
        CrossroadType typeUpdated = CrossroadType.PRIVATE;
        List<String> roadIDsUpdated = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        roadIDs.add("234");
        List<String> collisionIDsUpdated = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        collisionIDs.add("gfd");

        Exception exception = assertThrows(Exception.class, () -> {
            crossroadService.updateCrossroad(id, nameUpdated, locationUpdated, ownerIdUpdated, typeUpdated, roadIDsUpdated, collisionIDsUpdated);
            crossroadService.deleteCrossroadById(id);
        });

        assertEquals(1, crossroadService.crossroadRepository.count());
        assertEquals("Cannot update crossroad with id: " + id + " because it does not exist.", exception.getMessage());
    }
}