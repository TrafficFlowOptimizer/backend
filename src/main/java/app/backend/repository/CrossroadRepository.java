package app.backend.repository;

import app.backend.document.crossroad.Crossroad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrossroadRepository extends MongoRepository<Crossroad, String> {
    Iterable<Crossroad> findAllByCreator(String creatorId);
}
