package app.backend.service;

import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import app.backend.repository.CrossroadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CrossroadServiceTestOnMocks {
    @InjectMocks
    private CrossroadService crossroadService;

    @Mock
    private CrossroadRepository crossroadRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetCrossroadsByCreatorIdOrPublic_forPublicCrossroads() {
        Crossroad publicCrossroad = new Crossroad("Grunwaldzkie", "Cracow", "otherId", CrossroadType.PUBLIC, null, null, null, null, null);
        when(crossroadRepository.findAll()).thenReturn(List.of(publicCrossroad));

        List<Crossroad> result = crossroadService.getCrossroadsByCreatorIdOrPublic("someId");

        assertEquals(1, result.size());
        assertEquals(CrossroadType.PUBLIC, result.get(0).getType());
    }

    @Test
    void testGetCrossroadsByCreatorIdOrPublic_forMatchingCreatorId() {
        String myId = "myId";
        Crossroad crossroadWithMatchingCreator = new Crossroad("Grunwaldzkie", "Cracow", myId, CrossroadType.PUBLIC, null, null, null, null, null);
        when(crossroadRepository.findAll()).thenReturn(List.of(crossroadWithMatchingCreator));

        List<Crossroad> result = crossroadService.getCrossroadsByCreatorIdOrPublic(myId);

        assertEquals(1, result.size());
        assertEquals(myId, result.get(0).getCreatorId());
    }

    @Test
    void testGetCrossroadsByCreatorIdOrPublic_forNonMatchingCreatorIdAndNotPublic() {
        Crossroad privateCrossroad = new Crossroad("Grunwaldzkie", "Cracow", "anotherCreator", CrossroadType.PRIVATE, null, null, null, null, null);
        when(crossroadRepository.findAll()).thenReturn(List.of(privateCrossroad));

        List<Crossroad> result = crossroadService.getCrossroadsByCreatorIdOrPublic("myId");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCrossroadsByCreatorIdOrPublic_forMultipleCrossroads() {
        Crossroad publicCrossroad = new Crossroad("Grunwaldzkie", "Cracow", "anotherCreator", CrossroadType.PUBLIC, null, null, null, null, null);

        Crossroad crossroadWithMatchingCreator = new Crossroad("Grunwaldzkie", "Cracow", "creator123", CrossroadType.PRIVATE, null, null, null, null, null);

        Crossroad privateCrossroad = new Crossroad("Grunwaldzkie", "Cracow", "anotherCreator", CrossroadType.PRIVATE, null, null, null, null, null);

        when(crossroadRepository.findAll()).thenReturn(Arrays.asList(publicCrossroad, crossroadWithMatchingCreator, privateCrossroad));

        List<Crossroad> result = crossroadService.getCrossroadsByCreatorIdOrPublic("creator123");

        assertEquals(2, result.size());
    }

}