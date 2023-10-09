package app.backend.service;

import app.backend.document.collision.Collision;
import app.backend.document.collision.CollisionType;
import app.backend.repository.CollisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CollisionService {

    private final CollisionRepository collisionRepository;

    @Autowired
    public CollisionService(CollisionRepository collisionRepository) {
        this.collisionRepository = collisionRepository;
    }

    public Collision getCollisionById(String id) {
        return collisionRepository
                .findById(id)
                .orElse(null);
    }

    public Collision addCollision(
            int index,
            String name,
            String trafficLight1Id,
            String trafficLight2Id,
            CollisionType type
    ){
        return collisionRepository.insert(
                new Collision(
                        index,
                        name,
                        trafficLight1Id,
                        trafficLight2Id,
                        type
                )
        );
    }

    public Collision deleteCollisionById(String id) {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()) {
            return null;
        }

        collisionRepository.deleteById(id);
        return collision.get();
    }

    public Collision updateCollision(
            String id,
            int index,
            String name,
            String trafficLight1Id,
            String trafficLight2Id,
            CollisionType type
    ) {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()){
            return null;
        }

        Collision collisionToUpdate = collision.get();
        collisionToUpdate.setIndex(index);
        collisionToUpdate.setName(name);
        collisionToUpdate.setTrafficLight1Id(trafficLight1Id);
        collisionToUpdate.setTrafficLight2Id(trafficLight2Id);
        collisionToUpdate.setType(type);

        collisionRepository.save(collisionToUpdate);

        return collisionToUpdate;
    }

    public CollisionRepository getCollisionRepository() {
        return collisionRepository;
    }
}
