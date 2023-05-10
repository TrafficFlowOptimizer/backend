package app.backend.service;

import app.backend.document.road.Road;
import app.backend.document.road.RoadType;
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
class RoadServiceTest {

    @Autowired
    private RoadService roadService;

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
            roadService.roadRepository.deleteAll();
        }

    @Test
    public void getRoadById_improperRoad_roadNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            roadService.getRoadById(id);
        });

        assertEquals(0, roadService.roadRepository.count());
        assertEquals("Cannot get road with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getRoadById_properRoad_correctRoad() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);
        roadService.addRoad("Notjohn", RoadType.TARGET, 12222);

        Road found = null;
        try {
            found = roadService.getRoadById(road.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, roadService.roadRepository.count());
        assertNotNull(found);
        assertEquals(name, found.getName());
        assertEquals(type, found.getType());
        assertEquals(capacity, found.getCapacity());
    }

    @Test
    public void addRoad_properRoad_roadAdded() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);

        assertEquals(1, roadService.roadRepository.count());
        assertEquals(name, road.getName());
        assertEquals(type, road.getType());
        assertEquals(capacity, road.getCapacity());
    }

    @Test
    public void deleteRoadById_properRoad_roadDeleted() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);

        String id = road.getId();
        try {
            roadService.deleteRoadById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            roadService.getRoadById(id);
        });

        assertEquals(0, roadService.roadRepository.count());
        assertEquals("Cannot get road with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteRoadById_improperRoad_roadNotFound() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            roadService.deleteRoadById(id);
        });

        assertEquals(1, roadService.roadRepository.count());
        assertEquals("Cannot delete road with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateRoad_properRoad_roadUpdated() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);

        String id = road.getId();
        String nameUpdated = "Jon";
        RoadType typeUpdated = RoadType.INTER;
        int capacityUpdated = 11;

        Road updated = null;
        try {
            roadService.updateRoad(id, nameUpdated, typeUpdated, capacityUpdated);
            updated = roadService.getRoadById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, roadService.roadRepository.count());
        assertNotNull(updated);
        assertEquals(nameUpdated, updated.getName());
        assertEquals(typeUpdated, updated.getType());
        assertEquals(capacityUpdated, updated.getCapacity());
    }

    @Test
    public void updateRoad_improperRoad_roadNotFound() {
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;

        Road road = roadService.addRoad(name, type, capacity);

        String id = "";
        String nameUpdated = "Jon";
        RoadType typeUpdated = RoadType.INTER;
        int capacityUpdated = 11;

        Exception exception = assertThrows(Exception.class, () -> {
            roadService.updateRoad(id, nameUpdated, typeUpdated, capacityUpdated);
            roadService.deleteRoadById(id);
        });

        assertEquals(1, roadService.roadRepository.count());
        assertEquals("Cannot update road with id: " + id + " because it does not exist.", exception.getMessage());
    }
}