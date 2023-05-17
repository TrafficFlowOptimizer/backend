package app.backend.service;

import app.backend.document.CarFlow;
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

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarFlowServiceTest {

    @Autowired
    private CarFlowService carFlowService;

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
        carFlowService.carFlowRepository.deleteAll();
    }

    @Test
    public void getCarFlowById_improperCarFlow_carFlowNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.getCarFlowById(id);
        });

        assertEquals(0, carFlowService.carFlowRepository.count());
        assertEquals("Cannot get carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getCarFlowById_properCarFlow_correctCarFlow() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);
        carFlowService.addCarFlow(11, LocalTime.ofSecondOfDay(12), LocalTime.ofSecondOfDay(22));

        CarFlow found = null;
        try {
            found = carFlowService.getCarFlowById(carFlow.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, carFlowService.carFlowRepository.count());
        assertNotNull(found);
        assertEquals(flow, found.getCarFlow());
        assertEquals(start, found.getStartTime());
        assertEquals(end, found.getEndTime());
    }

    @Test
    public void addCarFlow_properCarFlow_carFlowAdded() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);

        assertEquals(1, carFlowService.carFlowRepository.count());
        assertEquals(flow, carFlow.getCarFlow());
        assertEquals(start, carFlow.getStartTime());
        assertEquals(end, carFlow.getEndTime());
    }

    @Test
    public void deleteCarFlowById_properCarFlow_carFlowDeleted() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);

        String id = carFlow.getId();
        try {
            carFlowService.deleteCarFlowById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.getCarFlowById(id);
        });

        assertEquals(0, carFlowService.carFlowRepository.count());
        assertEquals("Cannot get carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteCarFlowById_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.deleteCarFlowById(id);
        });

        assertEquals(1, carFlowService.carFlowRepository.count());
        assertEquals("Cannot delete carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateCarFlow_properCarFlow_carFlowUpdated() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);

        String id = carFlow.getId();
        int flowUpdated = 13;
        LocalTime startUpdated = LocalTime.ofSecondOfDay(1800);
        LocalTime endUpdated = LocalTime.ofSecondOfDay(2000);

        CarFlow updated = null;
        try {
            carFlowService.updateCarFlow(id, flowUpdated, startUpdated, endUpdated);
            updated = carFlowService.getCarFlowById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, carFlowService.carFlowRepository.count());
        assertNotNull(updated);
        assertEquals(flowUpdated, updated.getCarFlow());
        assertEquals(startUpdated, updated.getStartTime());
        assertEquals(endUpdated, updated.getEndTime());
    }

    @Test
    public void updateCarFlow_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        LocalTime start = LocalTime.ofSecondOfDay(0);
        LocalTime end = LocalTime.ofSecondOfDay(1600);

        CarFlow carFlow = carFlowService.addCarFlow(flow, start, end);

        String id = "";
        int flowUpdated = 13;
        LocalTime startUpdated = LocalTime.ofSecondOfDay(1800);
        LocalTime endUpdated = LocalTime.ofSecondOfDay(2000);

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.updateCarFlow(id, flowUpdated, startUpdated, endUpdated);
            carFlowService.deleteCarFlowById(id);
        });

        assertEquals(1, carFlowService.carFlowRepository.count());
        assertEquals("Cannot update carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }
}