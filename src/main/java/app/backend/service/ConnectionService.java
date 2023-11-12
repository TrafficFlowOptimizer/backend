package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.document.Connection;
import app.backend.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final CarFlowService carFlowService;

    @Autowired
    public ConnectionService(ConnectionRepository connectionRepository, CarFlowService carFlowService) {
        this.connectionRepository = connectionRepository;
        this.carFlowService = carFlowService;
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

    //TODO: time interval string - value/enum/..?
    public CarFlow getNewestCarFlowByTimeIntervalIdForConnection(String connectionId, String timeIntervalId){
        return getConnectionById(connectionId).getCarFlowIds()
                .stream()
                .map(carFlowService::getCarFlowById)
                .filter(carFlow ->
                        Objects.equals(carFlow.getTimeIntervalId(), timeIntervalId))
                .max(Comparator.comparing(CarFlow::getVersion))
                .orElse(null);


    }

    public ConnectionRepository getConnectionRepository() {
        return connectionRepository;
    }
}
