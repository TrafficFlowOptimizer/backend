package app.backend.repository;

import app.backend.document.crossroad.Crossroad;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CrossroadRepository extends MongoRepository<Crossroad, String> {
}
