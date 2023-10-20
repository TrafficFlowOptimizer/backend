package app.backend.service;

import app.backend.document.road.Road;
import app.backend.document.road.RoadType;
import app.backend.repository.RoadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoadService {

    private final RoadRepository roadRepository;

    @Autowired
    public RoadService(RoadRepository roadRepository) {
        this.roadRepository = roadRepository;
    }

    public Road getRoadById(String id) {
        return roadRepository
                .findById(id)
                .orElse(null);
    }

    public Road addRoad(int index, String name, RoadType type, int capacity, float xCord, float yCord) {
        return roadRepository.insert(
                new Road(
                        index,
                        name,
                        type,
                        capacity,
                        xCord,
                        yCord
                )
        );
    }

    public Road deleteRoadById(String id) {
        Optional<Road> road = roadRepository.findById(id);
        if (road.isEmpty()) {
            return null;
        }

        roadRepository.deleteById(id);
        return road.get();
    }

    public Road updateRoad(String id, int index, String name, RoadType type, int capacity, float xCord, float yCord) {
        Optional<Road> road = roadRepository.findById(id);
        if (road.isEmpty()) {
            return null;
        }

        Road roadToUpdate = road.get();
        roadToUpdate.setIndex(index);
        roadToUpdate.setName(name);
        roadToUpdate.setType(type);
        roadToUpdate.setCapacity(capacity);
        roadToUpdate.setxCord(xCord);
        roadToUpdate.setyCord(yCord);

        roadRepository.save(roadToUpdate);

        return roadToUpdate;
    }

    public RoadRepository getRoadRepository() {
        return roadRepository;
    }
}
