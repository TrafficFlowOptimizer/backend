package app.backend.service;

import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import app.backend.repository.CrossroadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class CrossroadService {
    @Autowired
    CrossroadRepository crossroadRepository;

    public Crossroad getCrossroadById(String id) throws Exception {
        Optional<Crossroad> crossroad = crossroadRepository.findById(id);
        if (crossroad.isEmpty()){
            throw new Exception("Cannot get crossroad with id: " + id + " because it does not exist.");
        }

        return crossroad.get();
    }

    public List<Crossroad> getCrossroadByCreator(String creatorId) {
        Iterable<Crossroad> crossroads = crossroadRepository.findAllByCreator(creatorId);
        List<Crossroad> intersections = new LinkedList<>();
        for(Crossroad crossroad : crossroads) {
            intersections.add(crossroad);
        }

        return intersections;
    }

    public Crossroad addCrossroad(String name, String location, String ownerId, CrossroadType type, List<String> roadIDs, List<String> collisionIDs) {
        return crossroadRepository.insert(new Crossroad(name, location, ownerId, type, roadIDs, collisionIDs));
    }

    public Crossroad deleteCrossroadById(String id) throws Exception {
        Optional<Crossroad> crossroad = crossroadRepository.findById(id);
        if (crossroad.isEmpty()) {
            throw new Exception("Cannot delete crossroad with id: " + id + " because it does not exist.");
        }
        crossroadRepository.deleteById(id);

        return crossroad.get();
    }

    public Crossroad updateCrossroad(String id, String name, String location, String creator, CrossroadType type, List<String> roadIDs, List<String> collisionIDs) throws Exception {
        Optional<Crossroad> crossroad = crossroadRepository.findById(id);
        if (crossroad.isEmpty()){
            throw new Exception("Cannot update crossroad with id: " + id + " because it does not exist.");
        }
        Crossroad crossroadToUpdate = crossroad.get();

        crossroadToUpdate.setName(name);
        crossroadToUpdate.setLocation(location);
        crossroadToUpdate.setCreator(creator);
        crossroadToUpdate.setType(type);
        crossroadToUpdate.setRoadIds(roadIDs);
        crossroadToUpdate.setCollisionIds(collisionIDs);

        crossroadRepository.save(crossroadToUpdate);

        return crossroadToUpdate;
    }
}
