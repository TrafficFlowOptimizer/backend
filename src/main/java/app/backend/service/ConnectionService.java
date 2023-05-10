package app.backend.service;

import app.backend.document.Connection;
import app.backend.repository.ConnectionRepository;
import app.backend.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {
    @Autowired
    ConnectionRepository connectionRepository;

    public Connection getConnectionById(String id) throws Exception {
        Optional<Connection> connection = connectionRepository.findById(id);
        if (connection.isEmpty()){
            throw new Exception("Cannot get connection with id: " + id + " because it does not exist.");
        }

        return connection.get();
    }

    public Connection addConnection(List<String> trafficLightIDs, String sourceId, String targetId, List<String> carFlowIDs){
        return connectionRepository.insert(new Connection(trafficLightIDs, sourceId, targetId, carFlowIDs));
    }

    public Connection deleteConnectionById(String id) throws Exception {
        Optional<Connection> connection = connectionRepository.findById(id);
        if (connection.isEmpty()) {
            throw new Exception("Cannot delete connection with id: " + id + " because it does not exist.");
        }
        connectionRepository.deleteById(id);
        return connection.get();
    }

    public Connection updateConnection(String id, List<String> trafficLightIDs, String sourceId, String targetId, List<String> carFlowIDs) throws Exception {
        Optional<Connection> connection = connectionRepository.findById(id);
        if (connection.isEmpty()){
            throw new Exception("Cannot update connection with id: " + id + " because it does not exist.");
        }
        Connection connectionToUpdate = connection.get();

        connectionToUpdate.setTrafficLightIds(trafficLightIDs);
        connectionToUpdate.setSourceId(sourceId);
        connectionToUpdate.setTargetId(targetId);
        connectionToUpdate.setCarFlowIds(carFlowIDs);

        connectionRepository.save(connectionToUpdate);

        return connectionToUpdate;
    }
}
