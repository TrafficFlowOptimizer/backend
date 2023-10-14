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
        trafficLightService.addTrafficLight(0, FORWARD);
        int index = 1;
        String name = "name";
        TrafficLightType type = LEFT;
        TrafficLight trafficLight = trafficLightService.addTrafficLight(index, type);
        trafficLightService.addTrafficLight(2, RIGHT);

        String id = trafficLight.getId();
        TrafficLight found = trafficLightService.getTrafficLightById(id);

        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
        assertEquals(id, found.getId());
        assertEquals(index, found.getIndex());
        assertEquals(type, found.getType());
    }

    @Test
    void getAndGetTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight(0, LEFT);
        trafficLightService.addTrafficLight(1, FORWARD);
        trafficLightService.addTrafficLight(2, RIGHT);

        String id = "";
        assertNull(trafficLightService.getTrafficLightById(id));
        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
    }

    @Test
    void deleteTrafficLightById_properTrafficLight_trafficLightDeleted() {
        trafficLightService.addTrafficLight(0, LEFT);
        TrafficLight trafficLight = trafficLightService.addTrafficLight(1, FORWARD);
        trafficLightService.addTrafficLight(2, RIGHT);

        String id = trafficLight.getId();
        trafficLightService.deleteTrafficLightById(id);

        assertNull(trafficLightService.getTrafficLightById(id));
        assertEquals(2, trafficLightService.getTrafficLightRepository().count());
    }

    @Test
    void deleteTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight(0, LEFT);
        trafficLightService.addTrafficLight(1, FORWARD);
        trafficLightService.addTrafficLight(2, RIGHT);

        String id = "";
        assertNull(trafficLightService.deleteTrafficLightById(id));
        assertEquals(3, trafficLightService.getTrafficLightRepository().count());
    }

    @Test
    void updateTrafficLightById_properTrafficLight_trafficLightUpdated() {
        int index = 0;

        TrafficLight trafficLight = trafficLightService.addTrafficLight(index, LEFT);

        String id = trafficLight.getId();
        int indexUpdated = 1;

        trafficLightService.updateTrafficLight(id, indexUpdated, FORWARD);

        TrafficLight updated = trafficLightService.getTrafficLightById(id);

        assertEquals(1, trafficLightService.getTrafficLightRepository().count());
        assertNotNull(updated);
        assertEquals(indexUpdated, updated.getIndex());
        assertEquals(FORWARD, updated.getType());
    }

    @Test
    void updateTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        int index = 0;
        TrafficLightType type = LEFT;

        trafficLightService.addTrafficLight(index, type);

        String id = "";
        int indexUpdated = 1;

        assertNull(trafficLightService.updateTrafficLight(id, indexUpdated, RIGHT));

        trafficLightService.deleteTrafficLightById(id);

        assertEquals(1, trafficLightService.getTrafficLightRepository().count());
    }
}