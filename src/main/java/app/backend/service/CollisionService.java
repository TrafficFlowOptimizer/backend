package app.backend.service;

import app.backend.document.collision.Collision;
import app.backend.document.collision.CollisionType;
import app.backend.repository.CollisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class CollisionService {
    @Autowired
    CollisionRepository collisionRepository;

    public Collision getCollisionById(String id) throws Exception {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()){
            throw new Exception("Cannot get collision with id: " + id + " because it does not exist.");
        }

        return collision.get();
    }

    public Collision addCollision(String trafficLight1Id, String trafficLight2Id, CollisionType type){
        return collisionRepository.insert(new Collision(trafficLight1Id, trafficLight2Id, type));
    }

    public Collision deleteCollisionById(String id) throws Exception {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()) {
            throw new Exception("Cannot delete collision with id: " + id + " because it does not exist.");
        }
        collisionRepository.deleteById(id);
        return collision.get();
    }

    public Collision updateCollision(String id, String trafficLight1Id, String trafficLight2Id, CollisionType type) throws Exception {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()){
            throw new Exception("Cannot update collision with id: " + id + " because it does not exists");
        }
        Collision collisionToUpdate = collision.get();

        collisionToUpdate.setTrafficLight1Id(trafficLight1Id);
        collisionToUpdate.setTrafficLight2Id(trafficLight2Id);
        collisionToUpdate.setType(type);

        collisionRepository.save(collisionToUpdate);

        return collisionToUpdate;
    }
}
