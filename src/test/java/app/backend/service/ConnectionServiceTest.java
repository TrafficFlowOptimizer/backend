package app.backend.service;

import app.backend.document.Connection;
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
class ConnectionServiceTest {

    @Autowired
    private ConnectionService connectionService;

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
        connectionService.connectionRepository.deleteAll();
    }

    @Test
    public void getConnectionById_improperConnection_connectionNotFound() {
        String id = "";
        Exception exception = assertThrows(Exception.class, () -> {
            connectionService.getConnectionById(id);
        });

        assertEquals(0, connectionService.connectionRepository.count());
        assertEquals("Cannot get connection with id: " + id + " because it does not exist.", exception.getMessage());
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

        Connection found = null;
        try {
            found = connectionService.getConnectionById(connection.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, connectionService.connectionRepository.count());
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

        assertEquals(2, connectionService.connectionRepository.count());
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
        try {
            connectionService.deleteConnectionById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Exception exception = assertThrows(Exception.class, () -> {
            connectionService.getConnectionById(id);
        });

        assertEquals(1, connectionService.connectionRepository.count());
        assertEquals("Cannot get connection with id: " + id + " because it does not exist.", exception.getMessage());
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

        Connection connection = connectionService.addConnection(index, name, trafficLightIDs, sourceId, targetId, carFlowIDs);
        connectionService.addConnection(1, "a", new ArrayList<>(), "Notdoe", "sdf", new ArrayList<>());
        String id = "";

        Exception exception = assertThrows(Exception.class, () -> {
            connectionService.deleteConnectionById(id);
        });

        assertEquals(2, connectionService.connectionRepository.count());
        assertEquals("Cannot delete connection with id: " + id + " because it does not exist.", exception.getMessage());
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

        Connection updated = null;
        try {
            connectionService.updateConnection(id, indexUpdated, nameUpdated, trafficLightIDsUpdated, sourceIdUpdated, targetIdUpdated, carFlowIDsUpdated);
            updated = connectionService.getConnectionById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(2, connectionService.connectionRepository.count());
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

        Exception exception = assertThrows(Exception.class, () -> {
            connectionService.updateConnection(id, indexUpdated, nameUpdated, trafficLightIDsUpdated, sourceIdUpdated, targetIdUpdated, carFlowIDsUpdated);
            connectionService.deleteConnectionById(id);
        });

        assertEquals(1, connectionService.connectionRepository.count());
        assertEquals("Cannot update connection with id: " + id + " because it does not exist.", exception.getMessage());
    }
}