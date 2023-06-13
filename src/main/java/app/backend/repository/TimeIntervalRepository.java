package app.backend.repository;

import app.backend.document.TimeInterval;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeIntervalRepository extends MongoRepository<TimeInterval, String> {

}
