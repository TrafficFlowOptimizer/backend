package app.backend.service;

import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import app.backend.repository.CrossroadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CrossroadService {

    private final CrossroadRepository crossroadRepository;

    @Autowired
    public CrossroadService(CrossroadRepository crossroadRepository) {
        this.crossroadRepository = crossroadRepository;
    }

    public Crossroad getCrossroadById(String id) {
        return crossroadRepository
                .findById(id)
                .orElse(null);
    }

    public List<Crossroad> getCrossroadByCreatorId(String creatorId) {
        Iterable<Crossroad> crossroads = crossroadRepository.findAllByCreatorId(creatorId);

        return StreamSupport
                .stream(crossroads.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * @author Chat GPT4
     */
    public List<Crossroad> getCrossroadsByCreatorIdOrPublic(String creatorId) {
        List<Crossroad> crossroads = crossroadRepository.findAll();

        return crossroads.stream()
                .filter(crossroad -> crossroad.getType().equals(CrossroadType.PUBLIC) || crossroad.getCreatorId().equals(creatorId))
                .collect(Collectors.toList());
    }

    public List<Crossroad> getPublicCrossroads() {
        Iterable<Crossroad> crossroads = crossroadRepository.findAllByType(CrossroadType.PUBLIC);

        return StreamSupport
                .stream(crossroads.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Crossroad addCrossroad(String name, String location, String ownerId, CrossroadType type, List<String> roadIDs, List<String> collisionIDs, List<String> connectionIds, List<String> trafficLightIds) {
        return crossroadRepository.insert(
                new Crossroad(
                        name,
                        location,
                        ownerId,
                        type,
                        roadIDs,
                        collisionIDs,
                        connectionIds,
                        trafficLightIds
                )
        );
    }

    public Crossroad deleteCrossroadById(String id) {
        Optional<Crossroad> crossroad = crossroadRepository.findById(id);
        if (crossroad.isEmpty()) {
            return null;
        }

        crossroadRepository.deleteById(id);
        return crossroad.get();
    }

    public Crossroad updateCrossroad(
            String id,
            String name,
            String location,
            String creatorId,
            CrossroadType type,
            List<String> roadIDs,
            List<String> collisionIDs,
            List<String> connectionIds,
            List<String> trafficLightIds
    ) {
        Optional<Crossroad> crossroad = crossroadRepository.findById(id);
        if (crossroad.isEmpty()) {
            return null;
        }

        Crossroad crossroadToUpdate = crossroad.get();
        crossroadToUpdate.setName(name);
        crossroadToUpdate.setLocation(location);
        crossroadToUpdate.setCreatorId(creatorId);
        crossroadToUpdate.setType(type);
        crossroadToUpdate.setRoadIds(roadIDs);
        crossroadToUpdate.setCollisionIds(collisionIDs);
        crossroadToUpdate.setCollisionIds(connectionIds);
        crossroadToUpdate.setCollisionIds(trafficLightIds);

        crossroadRepository.save(crossroadToUpdate);

        return crossroadToUpdate;
    }

    public CrossroadRepository getCrossroadRepository() {
        return crossroadRepository;
    }
}
