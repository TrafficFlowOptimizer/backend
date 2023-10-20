package app.backend.repository;

import app.backend.document.crossroad.Crossroad;
import app.backend.document.crossroad.CrossroadType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrossroadRepository extends MongoRepository<Crossroad, String> {
    Iterable<Crossroad> findAllByCreatorId(String creatorId);

    Iterable<Crossroad> findAllByType(CrossroadType type);
}
