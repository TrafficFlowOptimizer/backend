package app.backend.service;

import app.backend.document.TrafficLight;
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
class TrafficLightServiceTest {

    @Autowired
    private TrafficLightService trafficLightService;

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
        trafficLightService.trafficLightRepository.deleteAll();
        }

    @Test
    void getAndGetTrafficLightById_properTrafficLight_correctTrafficLight() {
        trafficLightService.addTrafficLight();
        TrafficLight trafficLight = trafficLightService.addTrafficLight();
        trafficLightService.addTrafficLight();

        String id = trafficLight.getId();
        TrafficLight found = null;
        try {
            found = trafficLightService.getTrafficLightById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(3, trafficLightService.trafficLightRepository.count());
        assertEquals(id, found.getId());
    }

    @Test
    void getAndGetTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight();
        trafficLightService.addTrafficLight();
        trafficLightService.addTrafficLight();


        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.getTrafficLightById(id);
        });

        assertEquals(3, trafficLightService.trafficLightRepository.count());
        assertEquals("Cannot get trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    void deleteTrafficLightById_properTrafficLight_trafficLightDeleted() {
        trafficLightService.addTrafficLight();
        TrafficLight trafficLight = trafficLightService.addTrafficLight();
        trafficLightService.addTrafficLight();

        String id = trafficLight.getId();
        try {
            trafficLightService.deleteTrafficLightById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.getTrafficLightById(id);
        });

        assertEquals(2, trafficLightService.trafficLightRepository.count());
        assertEquals("Cannot get trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    void deleteTrafficLightById_improperTrafficLight_trafficLightNotFound() {
        trafficLightService.addTrafficLight();
        TrafficLight trafficLight = trafficLightService.addTrafficLight();
        trafficLightService.addTrafficLight();

        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            trafficLightService.deleteTrafficLightById(id);
        });

        assertEquals(3, trafficLightService.trafficLightRepository.count());
        assertEquals("Cannot delete trafficLight with id: " + id + " because it does not exist.", exception.getMessage());
    }
}