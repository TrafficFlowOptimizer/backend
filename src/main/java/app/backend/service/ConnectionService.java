package app.backend.service;

import app.backend.document.Connection;
import app.backend.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final CrossroadService crossroadService;

    @Autowired
    public ConnectionService(ConnectionRepository connectionRepository, CrossroadService crossroadService) {
        this.connectionRepository = connectionRepository;
        this.crossroadService = crossroadService;
    }

    public Connection getConnectionById(String id) {
        return connectionRepository
                .findById(id)
                .orElse(null);
    }

    public Connection addConnection(
            int index,
            String name,
            List<String> trafficLightIDs,
            String sourceId,
            String targetId,
            List<String> carFlowIDs
    ) {
        return connectionRepository.insert(
                new Connection(
                        index,
                        name,
                        trafficLightIDs,
                        sourceId,
                        targetId,
                        carFlowIDs
                )
        );
    }

    public Connection deleteConnectionById(String id) {
        Optional<Connection> connection = connectionRepository.findById(id);
        if (connection.isEmpty()) {
            return null;
        }

        connectionRepository.deleteById(id);
        return connection.get();
    }

    public Connection updateConnection(
            String id,
            int index,
            String name,
            List<String> trafficLightIDs,
            String sourceId,
            String targetId,
            List<String> carFlowIDs
    ) {
        Optional<Connection> connection = connectionRepository.findById(id);
        if (connection.isEmpty()) {
            return null;
        }

        Connection connectionToUpdate = connection.get();
        connectionToUpdate.setIndex(index);
        connectionToUpdate.setName(name);
        connectionToUpdate.setTrafficLightIds(trafficLightIDs);
        connectionToUpdate.setSourceId(sourceId);
        connectionToUpdate.setTargetId(targetId);
        connectionToUpdate.setCarFlowIds(carFlowIDs);

        connectionRepository.save(connectionToUpdate);

        return connectionToUpdate;
    }

//    public Connection updateConnectionAddCarFlowId(
//            String id,
//            String carFlowID
//    ) {
//        Optional<Connection> connection = connectionRepository.findById(id);
//        if (connection.isEmpty()) {
//            return null;
//        }
//
//        Connection connectionToUpdate = connection.get();
//        List<String> carFlowIDs = connectionToUpdate.getCarFlowIds();
//        carFlowIDs.add(carFlowID);
//        connectionToUpdate.setCarFlowIds(carFlowIDs);
//
//        connectionRepository.save(connectionToUpdate);
//
//        return connectionToUpdate;
//    }

    public List<Connection> getConnectionsOutByRoadId(String crossroadId, String roadId) {
        return crossroadService.getCrossroadById(crossroadId).getConnectionIds()
                .stream()
                .map(connectionRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(connection -> connection.getSourceId().equals(roadId))
                .toList();
    }

    public ConnectionRepository getConnectionRepository() {
        return connectionRepository;
    }
}
