package app.backend.repository;

import app.backend.document.collision.Collision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollisionRepository extends MongoRepository<Collision, String> {
}
