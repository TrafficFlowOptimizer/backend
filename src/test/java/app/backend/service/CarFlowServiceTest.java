package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.repository.CarFlowRepository;
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

    private final CarFlowService carFlowService;

    @Autowired
    public CarFlowServiceTest(CarFlowService carFlowService){
        this.carFlowService = carFlowService;
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
        carFlowService.getCarFlowRepository().deleteAll();
    }

    @Test
    public void getCarFlowById_improperCarFlow_carFlowNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.getCarFlowById(id);
        });

        assertEquals(0, carFlowService.getCarFlowRepository().count());
        assertEquals("Cannot get carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void getCarFlowById_properCarFlow_correctCarFlow() {
        double flow = 7.0;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);
        carFlowService.addCarFlow(11, "asdsdaddsds");

        CarFlow found = null;
        try {
            found = carFlowService.getCarFlowById(carFlow.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, carFlowService.getCarFlowRepository().count());
        assertNotNull(found);
        assertEquals(flow, found.getCarFlow());
        assertEquals(timeIntervalId, found.getTimeIntervalId());
    }

    @Test
    public void addCarFlow_properCarFlow_carFlowAdded() {
        int flow = 7;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertEquals(flow, carFlow.getCarFlow());
        assertEquals(timeIntervalId, carFlow.getTimeIntervalId());
    }

    @Test
    public void deleteCarFlowById_properCarFlow_carFlowDeleted() {
        int flow = 7;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);

        String id = carFlow.getId();
        try {
            carFlowService.deleteCarFlowById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.getCarFlowById(id);
        });

        assertEquals(0, carFlowService.getCarFlowRepository().count());
        assertEquals("Cannot get carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void deleteCarFlowById_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.deleteCarFlowById(id);
        });

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertEquals("Cannot delete carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }

    @Test
    public void updateCarFlow_properCarFlow_carFlowUpdated() {
        int flow = 7;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);

        String id = carFlow.getId();
        int flowUpdated = 13;
        String timeIntervalIdUpdated = "updt";

        CarFlow updated = null;
        try {
            carFlowService.updateCarFlow(id, flowUpdated, timeIntervalIdUpdated);
            updated = carFlowService.getCarFlowById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertNotNull(updated);
        assertEquals(flowUpdated, updated.getCarFlow());
        assertEquals(timeIntervalIdUpdated, updated.getTimeIntervalId());
    }

    @Test
    public void updateCarFlow_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        String timeIntervalId = "timeIntervalId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, timeIntervalId);

        String id = "";
        int flowUpdated = 13;
        String timeIntervalIdUpdated = "updt";

        Exception exception = assertThrows(Exception.class, () -> {
            carFlowService.updateCarFlow(id, flowUpdated, timeIntervalIdUpdated);
            carFlowService.deleteCarFlowById(id);
        });

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertEquals("Cannot update carFlow with id: " + id + " because it does not exist.", exception.getMessage());
    }
}