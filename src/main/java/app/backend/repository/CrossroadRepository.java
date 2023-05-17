package app.backend.repository;

import app.backend.document.crossroad.Crossroad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrossroadRepository extends MongoRepository<Crossroad, String> {
    Iterable<Crossroad> findAllByCreatorId(String creatorId);
}
