package app.backend.service;

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

import static app.backend.document.time.Day.MONDAY;
import static app.backend.document.time.Hour.T0000;
import static app.backend.document.time.Hour.T0100;
import static app.backend.document.time.Hour.T0200;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OptimizationServiceTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);
    @Autowired
    private OptimizationService optimizationService;
    @Autowired
    private StartTimeService startTimeService;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?retryWrites=false");
    }

    @AfterEach
    public void cleanUpEach() {
        optimizationService.getOptimizationRepository().deleteAll();
        startTimeService.startTimeRepository().deleteAll();
    }

    @Test
    void getNewestOptimizationByCrossroadId() {
        List<List<Integer>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                row.add(1);
            }
            results.add(row);
        }

        String startTimeId1 = startTimeService.addStartTime(MONDAY, T0000).getId();
        String startTimeId2 = startTimeService.addStartTime(MONDAY, T0100).getId();
        String startTimeId3 = startTimeService.addStartTime(MONDAY, T0200).getId();
        optimizationService.addOptimization("0", 0, startTimeId1, results);
        optimizationService.addOptimization("0", 1, startTimeId1, results);
        optimizationService.addOptimization("0", 3, startTimeId2, results);
        optimizationService.addOptimization("0", 2, startTimeId3, results);
        optimizationService.addOptimization("1", 1, startTimeId3, results);

        assertEquals(1, optimizationService.getNewestOptimizationByCrossroadId("0", startTimeId1).getVersion());
        assertEquals(3, optimizationService.getNewestOptimizationByCrossroadId("0", startTimeId2).getVersion());
        assertEquals(1, optimizationService.getNewestOptimizationByCrossroadId("1", startTimeId3).getVersion());
    }

    @Test
    void getSecondNewestOptimizationByCrossroadId() {
        List<List<Integer>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                row.add(1);
            }
            results.add(row);
        }

        String startTimeId1 = startTimeService.addStartTime(MONDAY, T0000).getId();
        String startTimeId2 = startTimeService.addStartTime(MONDAY, T0100).getId();
        String startTimeId3 = startTimeService.addStartTime(MONDAY, T0200).getId();
        optimizationService.addOptimization("0", 0, startTimeId1, results);
        optimizationService.addOptimization("0", 1, startTimeId1, results);
        optimizationService.addOptimization("0", 1, startTimeId2, results);
        optimizationService.addOptimization("0", 0, startTimeId2, results);
        optimizationService.addOptimization("0", 1, startTimeId3, results);

        assertEquals(0, optimizationService.getSecondNewestOptimizationByCrossroadId("0", startTimeId1).getVersion());
        assertEquals(0, optimizationService.getSecondNewestOptimizationByCrossroadId("0", startTimeId2).getVersion());
        assertNull(optimizationService.getSecondNewestOptimizationByCrossroadId("0", startTimeId3));
    }
}