package app.backend.repository;

import app.backend.document.Optimization;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OptimizationRepository extends MongoRepository<Optimization, String> {
    Iterable<Optimization> findAllByCrossroadId(String crossroadId);
}
