package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.document.Connection;
import app.backend.repository.CarFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Service
public class CarFlowService {

    ConnectionService connectionService;

    private final CarFlowRepository carFlowRepository;

    @Autowired
    public CarFlowService(CarFlowRepository carFlowRepository, ConnectionService connectionService) {
        this.connectionService = connectionService;
        this.carFlowRepository = carFlowRepository;
    }

    public CarFlow getCarFlowById(String id) {
        return carFlowRepository
                .findById(id)
                .orElse(null);
    }

    public CarFlow addCarFlow(double carFlow, String startTimeId, String connectionId) {

        Connection connection = connectionService.getConnectionById(connectionId);
        Integer version = connection.getCarFlowIds().size();
        CarFlow newCarFlow = new CarFlow(
                carFlow,
                startTimeId,
                version
        );
        newCarFlow = carFlowRepository.insert(newCarFlow);

        connection.getCarFlowIds().add(newCarFlow.getId());
        connectionService.updateConnection(connectionId, connection.getIndex(), connection.getName(),
                connection.getTrafficLightIds(), connection.getSourceId(), connection.getTargetId(),
                connection.getCarFlowIds());

        return newCarFlow;
    }

    //Use only in populationg default Crossroad
    public CarFlow addCarFlow(double carFlow, String startTimeId, Integer version) {
        return carFlowRepository.insert(
                new CarFlow(
                        carFlow,
                        startTimeId,
                        version
                )
        );
    }

    public CarFlow deleteCarFlowById(String id) {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()) {
            return null;
        }

        carFlowRepository.deleteById(id);
        return carFlow.get();
    }

    public CarFlow updateCarFlow(String id, int carFlowPm, String startTimeId) {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()) {
            return null;
        }

        CarFlow carFlowToUpdate = carFlow.get();
        carFlowToUpdate.setCarFlow(carFlowPm);
        carFlowToUpdate.setStartTimeId(startTimeId);

        carFlowRepository.save(carFlowToUpdate);

        return carFlowToUpdate;
    }

    public CarFlowRepository getCarFlowRepository() {
        return carFlowRepository;
    }

    public CarFlow getNewestCarFlowByStartTimeIdForConnection(String connectionId, String startTimeId){
        return connectionService.getConnectionById(connectionId).getCarFlowIds()
                .stream()
                .map(this::getCarFlowById)
                .filter(carFlow ->
                        Objects.equals(carFlow.getStartTimeId(), startTimeId))
                .max(Comparator.comparing(CarFlow::getVersion))
                .orElse(null);
    }
}
