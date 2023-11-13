package app.backend.repository;

import app.backend.document.time.StartTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StartTimeRepository extends MongoRepository<StartTime, String> {

}
