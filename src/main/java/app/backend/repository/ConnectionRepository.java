package app.backend.repository;

import app.backend.document.Connection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConnectionRepository extends MongoRepository<Connection, String> {
}
