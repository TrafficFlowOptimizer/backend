package app.backend.service;

import app.backend.document.road.Road;
import app.backend.document.road.RoadType;
import app.backend.repository.RoadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoadService {
    @Autowired
    RoadRepository roadRepository;

    public Road getRoadById(String id) throws Exception {
        Optional<Road> road = roadRepository.findById(id);
        if (road.isEmpty()){
            throw new Exception("Cannot get road with id: " + id + " because it does not exist.");
        }

        return road.get();
    }

    public Road addRoad(int index, String name, RoadType type, int capacity){
        return roadRepository.insert(new Road(index, name, type, capacity));
    }

    public Road deleteRoadById(String id) throws Exception {
        Optional<Road> road = roadRepository.findById(id);
        if (road.isEmpty()) {
            throw new Exception("Cannot delete road with id: " + id + " because it does not exist.");
        }
        roadRepository.deleteById(id);
        return road.get();
    }

    public Road updateRoad(String id, int index, String name, RoadType type, int capacity) throws Exception {
        Optional<Road> road = roadRepository.findById(id);
        if (road.isEmpty()){
            throw new Exception("Cannot update road with id: " + id + " because it does not exist.");
        }
        Road roadToUpdate = road.get();

        roadToUpdate.setIndex(index);
        roadToUpdate.setName(name);
        roadToUpdate.setType(type);
        roadToUpdate.setCapacity(capacity);

        roadRepository.save(roadToUpdate);

        return roadToUpdate;
    }
}