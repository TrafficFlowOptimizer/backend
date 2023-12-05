package app.backend.service;

import app.backend.document.CarFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarFlowServiceTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);
    private final CarFlowService carFlowService;

    @Autowired
    public CarFlowServiceTest(CarFlowService carFlowService) {
        this.carFlowService = carFlowService;
    }

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        carFlowService.getCarFlowRepository().deleteAll();
    }

    @Test
    public void getCarFlowById_improperCarFlow_carFlowNotFound() {
        String id = "";
        assertNull(carFlowService.getCarFlowById(id));
        assertEquals(0, carFlowService.getCarFlowRepository().count());
    }

    @Test
    public void getCarFlowById_properCarFlow_correctCarFlow() {
        int flow = 7;
        String startTimeId = "startTimeId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, startTimeId, 1);
        carFlowService.addCarFlow(11, "asdsdaddsds", 1);

        CarFlow found = carFlowService.getCarFlowById(carFlow.getId());

        assertEquals(2, carFlowService.getCarFlowRepository().count());
        assertNotNull(found);
        assertEquals(flow, found.getCarFlow());
        assertEquals(startTimeId, found.getStartTimeId());
    }

    @Test
    public void addCarFlow_properCarFlow_carFlowAdded() {
        int flow = 7;
        String startTimeId = "startTimeId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, startTimeId, 1);

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertEquals(flow, carFlow.getCarFlow());
        assertEquals(startTimeId, carFlow.getStartTimeId());
    }

    @Test
    public void deleteCarFlowById_properCarFlow_carFlowDeleted() {
        int flow = 7;
        String startTimeId = "startTimeId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, startTimeId, 1);

        String id = carFlow.getId();
        carFlowService.deleteCarFlowById(id);

        assertNull(carFlowService.getCarFlowById(id));
        assertEquals(0, carFlowService.getCarFlowRepository().count());
    }

    @Test
    public void deleteCarFlowById_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        String startTimeId = "startTimeId";

        carFlowService.addCarFlow(flow, startTimeId, 1);
        String id = "";

        assertNull(carFlowService.deleteCarFlowById(id));
        assertEquals(1, carFlowService.getCarFlowRepository().count());
    }

    @Test
    public void updateCarFlow_properCarFlow_carFlowUpdated() {
        int flow = 7;
        String startTimeId = "startTimeId";

        CarFlow carFlow = carFlowService.addCarFlow(flow, startTimeId, 1);

        String id = carFlow.getId();
        int flowUpdated = 13;
        String startTimeIdUpdated = "updt";

        carFlowService.updateCarFlow(id, flowUpdated, startTimeIdUpdated);
        CarFlow updated = carFlowService.getCarFlowById(id);

        assertEquals(1, carFlowService.getCarFlowRepository().count());
        assertNotNull(updated);
        assertEquals(flowUpdated, updated.getCarFlow());
        assertEquals(startTimeIdUpdated, updated.getStartTimeId());
    }

    @Test
    public void updateCarFlow_improperCarFlow_carFlowNotFound() {
        int flow = 7;
        String startTimeId = "startTimeId";

        carFlowService.addCarFlow(flow, startTimeId, 1);

        String id = "";
        int flowUpdated = 13;
        String startTimeIdUpdated = "updt";

        assertNull(carFlowService.updateCarFlow(id, flowUpdated, startTimeIdUpdated));
        assertNull(carFlowService.deleteCarFlowById(id));
        assertEquals(1, carFlowService.getCarFlowRepository().count());
    }
}