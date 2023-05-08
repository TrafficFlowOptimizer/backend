package app.backend.repository;

import app.backend.document.collision.Collision;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CollisionRepository extends MongoRepository<Collision, String> {
}
