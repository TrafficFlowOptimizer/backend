package app.backend.repository;

import app.backend.document.TrafficLight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrafficLightRepository extends MongoRepository<TrafficLight, String> {
}
