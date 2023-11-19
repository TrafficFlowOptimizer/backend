package app.backend.service;

import app.backend.document.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConnectionServiceTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);
    private final ConnectionService connectionService;

    @Autowired
    public ConnectionServiceTest(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        connectionService.getConnectionRepository().deleteAll();
    }

    @Test
    public void getConnectionById_improperConnection_connectionNotFound() {
        String id = "";
        assertNull(connectionService.getConnectionById(id));
        assertEquals(0, connectionService.getConnectionRepository().count());
    }

    @Test
    public void getConnectionById_properConnection_correctConnection() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());

        Connection found = connectionService.getConnectionById(connection.getId());

        assertEquals(2, connectionService.getConnectionRepository().count());
        assertNotNull(found);
        assertEquals(index, found.getIndex());
        assertEquals(name, found.getName());
        assertEquals(trafficLightIDs, found.getTrafficLightIds());
        assertEquals(sourceId, found.getSourceId());
        assertEquals(targetId, found.getTargetId());
        assertEquals(carFlowIDs, found.getCarFlowIds());
    }

    @Test
    public void addConnection_properConnection_connectionAdded() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());

        assertEquals(2, connectionService.getConnectionRepository().count());
        assertEquals(index, connection.getIndex());
        assertEquals(name, connection.getName());
        assertEquals(trafficLightIDs, connection.getTrafficLightIds());
        assertEquals(sourceId, connection.getSourceId());
        assertEquals(targetId, connection.getTargetId());
        assertEquals(carFlowIDs, connection.getCarFlowIds());
    }

    @Test
    public void deleteConnectionById_properConnection_connectionDeleted() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());

        String id = connection.getId();
        connectionService.deleteConnectionById(id);

        assertNull(connectionService.getConnectionById(id));
        assertEquals(1, connectionService.getConnectionRepository().count());
    }

    @Test
    public void deleteConnectionById_improperConnection_connectionNotFound() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());

        String id = "";
        assertNull(connectionService.deleteConnectionById(id));
        assertEquals(2, connectionService.getConnectionRepository().count());
    }

    @Test
    public void updateConnection_properConnection_connectionUpdated() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());

        String id = connection.getId();
        int indexUpdated = 2;
        String nameUpdated = "nm";
        String sourceIdUpdated = "dsa";
        String targetIdUpdated = "ddsfdfs";
        List<String> trafficLightIDsUpdated = new ArrayList<>();
        trafficLightIDsUpdated.add("123");
        trafficLightIDsUpdated.add("234");
        trafficLightIDsUpdated.add("234");
        List<String> carFlowIDsUpdated = new ArrayList<>();
        carFlowIDsUpdated.add("dfg");
        carFlowIDsUpdated.add("gfd");
        carFlowIDsUpdated.add("gfd");

        connectionService.updateConnection(id, indexUpdated, nameUpdated, trafficLightIDsUpdated, sourceIdUpdated, targetIdUpdated, carFlowIDsUpdated);
        Connection updated = connectionService.getConnectionById(id);

        assertEquals(2, connectionService.getConnectionRepository().count());
        assertNotNull(updated);
        assertEquals(indexUpdated, updated.getIndex());
        assertEquals(nameUpdated, updated.getName());
        assertEquals(trafficLightIDsUpdated, updated.getTrafficLightIds());
        assertEquals(sourceIdUpdated, updated.getSourceId());
        assertEquals(targetIdUpdated, updated.getTargetId());
        assertEquals(carFlowIDsUpdated, updated.getCarFlowIds());
    }

    @Test
    public void updateConnection_improperConnection_connectionNotFound() {
        int index = 0;
        String name = "name";
        String sourceId = "abc";
        String targetId = "abc";
        List<String> trafficLightIDs = new ArrayList<>();
        trafficLightIDs.add("123");
        trafficLightIDs.add("234");
        List<String> carFlowIDs = new ArrayList<>();
        carFlowIDs.add("dfg");
        carFlowIDs.add("gfd");

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);

        String id = "";
        int indexUpdated = 1;
        String nameUpdated = "nm";
        String sourceIdUpdated = "dsa";
        String targetIdUpdated = "ddsfdfs";
        List<String> trafficLightIDsUpdated = new ArrayList<>();
        trafficLightIDsUpdated.add("123");
        trafficLightIDsUpdated.add("234");
        trafficLightIDsUpdated.add("234");
        List<String> carFlowIDsUpdated = new ArrayList<>();
        carFlowIDsUpdated.add("dfg");
        carFlowIDsUpdated.add("gfd");
        carFlowIDsUpdated.add("gfd");

        assertNull(connectionService.updateConnection(id, indexUpdated, nameUpdated, trafficLightIDsUpdated, sourceIdUpdated, targetIdUpdated, carFlowIDsUpdated));
        assertNull(connectionService.deleteConnectionById(id));
        assertEquals(1, connectionService.getConnectionRepository().count());
    }
}