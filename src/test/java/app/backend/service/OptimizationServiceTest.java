package app.backend.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OptimizationServiceTest {
    @Autowired
    private OptimizationService optimizationService;

    @AfterEach
    public void cleanUpEach(){
        optimizationService.optimizationRepository.deleteAll();
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

        optimizationService.addOptimization("0", 0, "10", results);
        optimizationService.addOptimization("0", 2, "11", results);
        optimizationService.addOptimization("0", 3, "12", results);
        optimizationService.addOptimization("0", 4, "12", results);
        optimizationService.addOptimization("1", 1, "11", results);


        assertEquals(2, optimizationService.getNewestOptimizationByCrossroadId("0", "11").getVersion());
        assertEquals(4, optimizationService.getNewestOptimizationByCrossroadId("0", "12").getVersion());
        assertEquals(1, optimizationService.getNewestOptimizationByCrossroadId("1", "11").getVersion());
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

        optimizationService.addOptimization("0", 0, "10", results);
        optimizationService.addOptimization("0", 2, "11", results);
        optimizationService.addOptimization("0", 3, "12", results);
        optimizationService.addOptimization("0", 4, "12", results);
        optimizationService.addOptimization("1", 1, "11", results);


        assertEquals(2, optimizationService.getSecondNewestOptimizationByCrossroadId("0", "11").getVersion());
        assertEquals(3, optimizationService.getSecondNewestOptimizationByCrossroadId("0", "12").getVersion());
        assertEquals(1, optimizationService.getSecondNewestOptimizationByCrossroadId("1", "11").getVersion());
    }
}