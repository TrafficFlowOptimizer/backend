package app.backend.repository;

import app.backend.document.light.TrafficLight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficLightRepository extends MongoRepository<TrafficLight, String> {

}
