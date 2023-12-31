package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.document.Connection;
import app.backend.repository.CarFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class CarFlowService {

    private final CarFlowRepository carFlowRepository;
    ConnectionService connectionService;
    CrossroadService crossroadService;

    @Autowired
    public CarFlowService(CarFlowRepository carFlowRepository, CrossroadService crossroadService, ConnectionService connectionService) {
        this.connectionService = connectionService;
        this.crossroadService = crossroadService;
        this.carFlowRepository = carFlowRepository;
    }

    public CarFlow getCarFlowById(String id) {
        return carFlowRepository
                .findById(id)
                .orElse(null);
    }

    public CarFlow addCarFlow(int carFlow, String startTimeId, String connectionId) {
        Connection connection = connectionService.getConnectionById(connectionId);
        int version = connection.getCarFlowIds().size();

        CarFlow newCarFlow = new CarFlow(
                carFlow,
                startTimeId,
                version
        );
        newCarFlow = carFlowRepository.insert(newCarFlow);

        connection.getCarFlowIds().add(newCarFlow.getId());
        connectionService.updateConnection(
                connectionId,
                connection.getIndex(),
                connection.getName(),
                connection.getTrafficLightIds(),
                connection.getSourceId(),
                connection.getTargetId(),
                connection.getCarFlowIds()
        );

        return newCarFlow;
    }

    //Use only in populationg default Crossroad
    public CarFlow addCarFlow(int carFlow, String startTimeId, int version) {
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

    public CarFlow getNewestCarFlowByStartTimeIdForConnection(String connectionId, String startTimeId) {
        return connectionService.getConnectionById(connectionId).getCarFlowIds()
                .stream()
                .map(carFlowRepository::findById)
                .flatMap(Optional::stream)
                .filter(carFlow -> carFlow.getStartTimeId().equals(startTimeId))
                .max(Comparator.comparing(CarFlow::getVersion))
                .orElse(null);
    }

    public HashMap<Integer, Integer> getConnectionIdxToCurrentCarFlowMapByStartTimeIdForCrossroad(String crossroadId, String startTimeId) {
        HashMap<Integer, Integer> response = new HashMap<>();

        crossroadService.getCrossroadById(crossroadId).getConnectionIds()
                .stream()
                .map(connectionService::getConnectionById)
                .forEach(connection -> {
                    List<CarFlow> carFlowList = connection.getCarFlowIds().stream()
                            .map(carFlowRepository::findById)
                            .flatMap(Optional::stream)
                            .filter(carFlow -> carFlow.getStartTimeId().equals(startTimeId))
                            .sorted(Comparator.comparing(CarFlow::getVersion).reversed())
                            .toList();
                    double summedCarFlow = 0;
                    double weight = 0;
                    if (carFlowList.size() == 1) {
                        summedCarFlow = carFlowList.get(0).getCarFlow();
                        weight = 1;
                    } else {
                        for (int i = 0; i < carFlowList.size() - 1; i++) {
                            summedCarFlow += carFlowList.get(i).getCarFlow() / (double) (i + 1);
                            weight += 1 / (double) (i + 1);
                        }
                    }
                    response.put(connection.getIndex(), (int) Math.round(summedCarFlow / weight));
                });
        return response;
    }
}
