package app.backend.repository;

import app.backend.document.CarFlow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarFlowRepository extends MongoRepository<CarFlow, String> {

}
