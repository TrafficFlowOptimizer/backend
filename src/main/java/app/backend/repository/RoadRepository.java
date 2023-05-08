package app.backend.repository;

import app.backend.document.road.Road;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoadRepository extends MongoRepository<Road, String> {
}
