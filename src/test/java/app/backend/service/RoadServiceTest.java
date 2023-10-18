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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoadServiceTest {

    private final RoadService roadService;

    @Autowired
    public RoadServiceTest(RoadService roadService) {
        this.roadService = roadService;
    }

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", ()-> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach(){
            roadService.getRoadRepository().deleteAll();
        }

    @Test
    public void getRoadById_improperRoad_roadNotFound() {
        String id = "";
        assertNull(roadService.getRoadById(id));
        assertEquals(0, roadService.getRoadRepository().count());
    }

    @Test
    public void getRoadById_properRoad_correctRoad() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        Road road = roadService.addRoad(index, name, type, capacity, x, y);
        roadService.addRoad(1, "Notjohn", RoadType.TARGET, 12222, (float)-0.1, (float)-0.2);

        Road found = roadService.getRoadById(road.getId());

        assertEquals(2, roadService.getRoadRepository().count());
        assertNotNull(found);
        assertEquals(index, found.getIndex());
        assertEquals(name, found.getName());
        assertEquals(type, found.getType());
        assertEquals(capacity, found.getCapacity());
        assertEquals(x, found.getxCord());
        assertEquals(y, found.getyCord());
    }

    @Test
    public void addRoad_properRoad_roadAdded() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        Road road = roadService.addRoad(index, name, type, capacity, x, y);

        assertEquals(1, roadService.getRoadRepository().count());
        assertEquals(index, road.getIndex());
        assertEquals(name, road.getName());
        assertEquals(type, road.getType());
        assertEquals(capacity, road.getCapacity());
        assertEquals(x, road.getxCord());
        assertEquals(y, road.getyCord());
    }

    @Test
    public void deleteRoadById_properRoad_roadDeleted() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        Road road = roadService.addRoad(index, name, type, capacity, x, y);

        String id = road.getId();
        roadService.deleteRoadById(id);

        assertNull(roadService.getRoadById(id));
        assertEquals(0, roadService.getRoadRepository().count());
    }

    @Test
    public void deleteRoadById_improperRoad_roadNotFound() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        roadService.addRoad(index, name, type, capacity, x, y);
        String id = "";

        assertNull(roadService.deleteRoadById(id));
        assertEquals(1, roadService.getRoadRepository().count());
    }

    @Test
    public void updateRoad_properRoad_roadUpdated() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        Road road = roadService.addRoad(index, name, type, capacity, x, y);

        String id = road.getId();
        int indexUpdated = 1;
        String nameUpdated = "Jon";
        RoadType typeUpdated = RoadType.INTER;
        int capacityUpdated = 11;
        float xUpdated = (float)0.2332;
        float yUpdated = (float)-11.233;

        roadService.updateRoad(id, indexUpdated, nameUpdated, typeUpdated, capacityUpdated, xUpdated, yUpdated);
        Road updated = roadService.getRoadById(id);

        assertEquals(1, roadService.getRoadRepository().count());
        assertNotNull(updated);
        assertEquals(indexUpdated, updated.getIndex());
        assertEquals(nameUpdated, updated.getName());
        assertEquals(typeUpdated, updated.getType());
        assertEquals(capacityUpdated, updated.getCapacity());
        assertEquals(xUpdated, updated.getxCord());
        assertEquals(yUpdated, updated.getyCord());
    }

    @Test
    public void updateRoad_improperRoad_roadNotFound() {
        int index = 0;
        String name = "John";
        RoadType type = RoadType.SOURCE;
        int capacity = 10;
        float x = (float)0.1;
        float y = (float)0.2;

        roadService.addRoad(index, name, type, capacity, x, y);

        String id = "";
        int indexUpdated = 1;
        String nameUpdated = "Jon";
        RoadType typeUpdated = RoadType.INTER;
        int capacityUpdated = 11;
        float xUpdated = (float)0.2332;
        float yUpdated = (float)-11.233;

        assertNull(roadService.updateRoad(id, indexUpdated, nameUpdated, typeUpdated, capacityUpdated, xUpdated, yUpdated));
        assertNull(roadService.deleteRoadById(id));
        assertEquals(1, roadService.getRoadRepository().count());
    }
}