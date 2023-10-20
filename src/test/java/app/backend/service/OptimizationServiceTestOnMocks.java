package app.backend.service;

import app.backend.document.Optimization;
import app.backend.repository.OptimizationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OptimizationServiceTestOnMocks {

    private static OptimizationService optimizationService;
    private static OptimizationRepository optimizationRepository;

    @BeforeAll
    public static void setUp() {
        optimizationRepository = mock(OptimizationRepository.class);
        optimizationService = new OptimizationService(optimizationRepository);
    }

    @Test
    void testGetFreeVersionNumber_emptyOptimizations() {
        when(optimizationRepository.findAll()).thenReturn(new ArrayList<>());

        int result = optimizationService.getFreeVersionNumber("dummyCrossroadId");
        assertEquals(0, result);

    }

    @Test
    void testGetFreeVersionNumber_SequentialVersions() {
        List<Optimization> sampleData = Arrays.asList(
                new Optimization("testId", 1, null, null),
                new Optimization("testId", 2, null, null),
                new Optimization("testId", 3, null, null)
        );

        when(optimizationRepository.findAllByCrossroadId("testId")).thenReturn(sampleData);

        int result = optimizationService.getFreeVersionNumber("testId");
        assertEquals(4, result);
    }

    @Test
    void testGetFreeVersionNumber_nonSequentialVersions() {
        List<Optimization> sampleData = Arrays.asList(
                new Optimization("testId", 1, null, null),
                new Optimization("testId", 2, null, null),
                new Optimization("testId", 5, null, null)
        );

        when(optimizationRepository.findAllByCrossroadId("testId")).thenReturn(sampleData);

        int result = optimizationService.getFreeVersionNumber("testId");
        assertEquals(6, result);
    }


    @Test
    public void testGetOptimizations_NoMatchingTimeInterval() {
        // Setup
        Optimization opt1 = new Optimization("1", 2, "timeB", null);
        Optimization opt2 = new Optimization("1", 3, "timeB", null);
        when(optimizationRepository.findAllByCrossroadId("1")).thenReturn(new LinkedList<>(Arrays.asList(opt1, opt2)));

        // Action
        Iterable<Optimization> result = optimizationService.getOptimizationsByCrossroadIdAndTimeInterval("1", "timeC");

        // Assertion
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void testGetOptimizations_MatchingTimeInterval() {
        // Setup
        Optimization opt1 = new Optimization("1", 1, "timeB", null);
        Optimization opt2 = new Optimization("1", 2, "timeB", null);
        Optimization opt3 = new Optimization("1", 2, "timeC", null);
        when(optimizationRepository.findAllByCrossroadId("1")).thenReturn(new LinkedList<>(Arrays.asList(opt1, opt2, opt3)));

        // Action
        Iterable<Optimization> result = optimizationService.getOptimizationsByCrossroadIdAndTimeInterval("1", "timeB");

        // Assertion
        assertEquals(opt1, ((LinkedList) result).get(0));
        assertEquals(opt2, ((LinkedList) result).get(1));
    }

    @Test
    public void testGetOptimizations_EmptyList() {
        // Setup
        when(optimizationRepository.findAllByCrossroadId("1")).thenReturn(Collections.emptyList());

        // Action
        Iterable<Optimization> result = optimizationService.getOptimizationsByCrossroadIdAndTimeInterval("1", "timeB");

        // Assertion
        assertFalse(result.iterator().hasNext());
    }
}
