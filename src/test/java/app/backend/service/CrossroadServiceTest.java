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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrossroadServiceTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);
    private final CrossroadService crossroadService;

    @Autowired
    public CrossroadServiceTest(CrossroadService crossroadService) {
        this.crossroadService = crossroadService;
    }

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        crossroadService.getCrossroadRepository().deleteAll();
    }

    @Test
    public void getCrossroadById_improperCrossroad_crossroadNotFound() {
        String id = "";
        assertNull(crossroadService.getCrossroadById(id));
        assertEquals(0, crossroadService.getCrossroadRepository().count());
    }

    @Test
    public void getCrossroadById_properCrossroad_correctCrossroad() {
        String name = "John";
        String location = "Doe";
        String creatorId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        Crossroad crossroad = crossroadService.addCrossroad(name, location, creatorId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);
        crossroadService.addCrossroad("Notjohn", "Notdoe", "sdf", CrossroadType.PUBLIC, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), imageId);

        Crossroad found = crossroadService.getCrossroadById(crossroad.getId());

        assertEquals(2, crossroadService.getCrossroadRepository().count());
        assertNotNull(found);
        assertEquals(name, found.getName());
        assertEquals(location, found.getLocation());
        assertEquals(creatorId, found.getCreatorId());
        assertEquals(type, found.getType());
        assertEquals(roadIDs, found.getRoadIds());
        assertEquals(collisionIDs, found.getCollisionIds());
        assertEquals(connectionIds, found.getConnectionIds());
        assertEquals(trafficLightIds, found.getTrafficLightIds());
        assertEquals(imageId, found.getImageId());
    }

    @Test
    public void addCrossroad_properCrossroad_crossroadAdded() {
        String name = "John";
        String location = "Doe";
        String creatorId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        Crossroad crossroad = crossroadService.addCrossroad(name, location, creatorId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);

        assertEquals(1, crossroadService.getCrossroadRepository().count());
        assertEquals(name, crossroad.getName());
        assertEquals(location, crossroad.getLocation());
        assertEquals(creatorId, crossroad.getCreatorId());
        assertEquals(type, crossroad.getType());
        assertEquals(roadIDs, crossroad.getRoadIds());
        assertEquals(collisionIDs, crossroad.getCollisionIds());
        assertEquals(connectionIds, crossroad.getConnectionIds());
        assertEquals(trafficLightIds, crossroad.getTrafficLightIds());
        assertEquals(imageId, crossroad.getImageId());
    }

    @Test
    public void deleteCrossroadById_properCrossroad_crossroadDeleted() {
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
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        Crossroad crossroad = crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);

        String id = crossroad.getId();
        crossroadService.deleteCrossroadById(id);

        assertNull(crossroadService.getCrossroadById(id));
        assertEquals(0, crossroadService.getCrossroadRepository().count());
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
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);

        String id = "";
        assertNull(crossroadService.deleteCrossroadById(id));
        assertEquals(1, crossroadService.getCrossroadRepository().count());
    }

    @Test
    public void updateCrossroad_properCrossroad_crossroadUpdated() {
        String name = "John";
        String location = "Doe";
        String creatorId = "abc";
        CrossroadType type = CrossroadType.PUBLIC;
        List<String> roadIDs = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        List<String> collisionIDs = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        Crossroad crossroad = crossroadService.addCrossroad(name, location, creatorId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);

        String id = crossroad.getId();
        String nameUpdated = "Johna";
        String locationUpdated = "Doea";
        String creatorIdUpdated = "abca";
        CrossroadType typeUpdated = CrossroadType.PRIVATE;
        List<String> roadIDsUpdated = new ArrayList<>();
        roadIDs.add("123");
        roadIDs.add("234");
        roadIDs.add("234");
        List<String> collisionIDsUpdated = new ArrayList<>();
        collisionIDs.add("dfg");
        collisionIDs.add("gfd");
        collisionIDs.add("gfd");
        List<String> connectionIdsUpdated = new ArrayList<>();
        collisionIDs.add("dsaads");
        collisionIDs.add("fjksla");
        List<String> trafficLightIdsUpdated = new ArrayList<>();
        collisionIDs.add("iwopeq");
        collisionIDs.add("als");
        String imageIdUpdated = "updatedid";

        crossroadService.updateCrossroad(id, nameUpdated, locationUpdated, creatorIdUpdated, typeUpdated, roadIDsUpdated, collisionIDsUpdated, connectionIdsUpdated, trafficLightIdsUpdated, imageIdUpdated);
        Crossroad updated = crossroadService.getCrossroadById(id);

        assertEquals(1, crossroadService.getCrossroadRepository().count());
        assertNotNull(updated);
        assertEquals(nameUpdated, updated.getName());
        assertEquals(locationUpdated, updated.getLocation());
        assertEquals(creatorIdUpdated, updated.getCreatorId());
        assertEquals(typeUpdated, updated.getType());
        assertEquals(roadIDsUpdated, updated.getRoadIds());
        assertEquals(collisionIDsUpdated, updated.getCollisionIds());
        assertEquals(connectionIds, updated.getConnectionIds());
        assertEquals(trafficLightIds, updated.getTrafficLightIds());
        assertEquals(imageIdUpdated, updated.getImageId());
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
        List<String> connectionIds = new ArrayList<>();
        collisionIDs.add("a");
        collisionIDs.add("v");
        List<String> trafficLightIds = new ArrayList<>();
        collisionIDs.add("s");
        collisionIDs.add("f");
        String imageId = "nosuchid";

        crossroadService.addCrossroad(name, location, ownerId, type, roadIDs, collisionIDs, connectionIds, trafficLightIds, imageId);

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
        List<String> connectionIdsUpdated = new ArrayList<>();
        collisionIDs.add("dsaads");
        collisionIDs.add("fjksla");
        List<String> trafficLightIdsUpdated = new ArrayList<>();
        collisionIDs.add("iwopeq");
        collisionIDs.add("als");
        String imageIdUpdated = "updatedid";

        assertNull(crossroadService.updateCrossroad(id, nameUpdated, locationUpdated, ownerIdUpdated, typeUpdated, roadIDsUpdated, collisionIDsUpdated, connectionIdsUpdated, trafficLightIdsUpdated, imageIdUpdated));
        assertNull(crossroadService.deleteCrossroadById(id));
        assertEquals(1, crossroadService.getCrossroadRepository().count());
    }
}