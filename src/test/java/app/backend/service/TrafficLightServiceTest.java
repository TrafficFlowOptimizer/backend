package app.backend.service;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
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

import static app.backend.document.light.TrafficLightType.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrafficLightServiceTest {

    private final TrafficLightService trafficLightService;

    @Autowired
    public TrafficLightServiceTest(TrafficLightService trafficLightService) {
        this.trafficLightService = trafficLightService;
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
        trafficLightService.getTrafficLightRepository().deleteAll();
        }

    @Test
    void getAndGetTrafficLightById_properTrafficLight_correctTrafficLight() {
        trafficLightService.addTrafficLight(0, "nm", FORWARD);
        int index = 1;
        String name = "name";
        TrafficLightType type = LEFT;
        TrafficLight trafficLight = trafficLightService.addTrafficLight(index, name, type);
        trafficLightService.addTrafficLight(2, "ndsam", RIGHT);

        String id = trafficLight.getId();
        TrafficLight found = null;
        try {
            found = trafficLightService.getTrafficLightById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
        assertEquals(id, found.getId());
        assertEquals(index, found.getIndex());
        assertEquals(name, found.getName());
        assertEquals(type, found.getType());
    }

    @Test
    void getAndGetTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight(0, "a", LEFT);
        trafficLightService.addTrafficLight(1, "b", FORWARD);
        trafficLightService.addTrafficLight(2, "c", RIGHT);


        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.getTrafficLightById(id);
        });

        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
        assertEquals("Cannot get trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    void deleteTrafficLightById_properTrafficLight_trafficLightDeleted() {
        trafficLightService.addTrafficLight(0, "a", LEFT);
        TrafficLight trafficLight = trafficLightService.addTrafficLight(1, "b", FORWARD);
        trafficLightService.addTrafficLight(2, "c", RIGHT);

        String id = trafficLight.getId();
        try {
            trafficLightService.deleteTrafficLightById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.getTrafficLightById(id);
        });

        assertEquals(2, trafficLightService.getTrafficLightRepository().count());
        assertEquals("Cannot get trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    void deleteTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight(0, "a", LEFT);
        TrafficLight trafficLight = trafficLightService.addTrafficLight(1, "name", FORWARD);
        trafficLightService.addTrafficLight(2, "c", RIGHT);

        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.deleteTrafficLightById(id);
        });

        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
        assertEquals("Cannot delete trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    void updateTrafficLightById_properTrafficLight_trafficLightUpdated() {
        int index = 0;
        String name = "name";

        TrafficLight trafficLight = trafficLightService.addTrafficLight(index, name, LEFT);

        String id = trafficLight.getId();
        int indexUpdated = 1;
        String nameUpdated = "names";

        TrafficLight updated = null;
        try {
            trafficLightService.updateTrafficLight(id, indexUpdated, nameUpdated, FORWARD);
            updated = trafficLightService.getTrafficLightById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, trafficLightService.getTrafficLightRepository().count());
        assertNotNull(updated);
        assertEquals(indexUpdated, updated.getIndex());
        assertEquals(nameUpdated, updated.getName());
        assertEquals(FORWARD, updated.getType());
    }

    @Test
    void updateTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        int index = 0;
        TrafficLightType type = LEFT;

        TrafficLight trafficLight = trafficLightService.addTrafficLight(index, "n", type);

        String id = "";
        int indexUpdated = 1;
        String nameUpdated = "name";
        TrafficLightType typeUpdated = RIGHT;

        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.updateTrafficLight(id, indexUpdated, nameUpdated, typeUpdated);
            trafficLightService.deleteTrafficLightById(id);
        });

        assertEquals(1, trafficLightService.getTrafficLightRepository().count());
        assertEquals("Cannot update trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }
}