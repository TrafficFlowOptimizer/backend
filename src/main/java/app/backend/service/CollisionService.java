package app.backend.service;

import app.backend.document.Collision;
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
            String connection1Id,
            String connection2Id,
            boolean bothCanBeOn
    ){
        return collisionRepository.insert(
                new Collision(
                        index,
                        name,
                        connection1Id,
                        connection2Id,
                        bothCanBeOn
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
            String connection1Id,
            String connection2Id,
            boolean bothCanBeOn
    ) {
        Optional<Collision> collision = collisionRepository.findById(id);
        if (collision.isEmpty()){
            return null;
        }

        Collision collisionToUpdate = collision.get();
        collisionToUpdate.setIndex(index);
        collisionToUpdate.setName(name);
        collisionToUpdate.setConnection1Id(connection1Id);
        collisionToUpdate.setConnection2Id(connection2Id);
        collisionToUpdate.setBothCanBeOn(bothCanBeOn);

        collisionRepository.save(collisionToUpdate);

        return collisionToUpdate;
    }

    public CollisionRepository getCollisionRepository() {
        return collisionRepository;
    }
}
