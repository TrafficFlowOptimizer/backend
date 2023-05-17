package app.backend.repository;

import app.backend.document.road.Road;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadRepository extends MongoRepository<Road, String> {

}
