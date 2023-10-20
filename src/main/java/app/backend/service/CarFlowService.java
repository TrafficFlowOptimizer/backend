package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.repository.CarFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarFlowService {

    private final CarFlowRepository carFlowRepository;

    @Autowired
    public CarFlowService(CarFlowRepository carFlowRepository) {
        this.carFlowRepository = carFlowRepository;
    }

    public CarFlow getCarFlowById(String id) {
        return carFlowRepository
                .findById(id)
                .orElse(null);
    }

    public CarFlow addCarFlow(double carFlow, String timeIntervalId) {
        return carFlowRepository.insert(
                new CarFlow(
                        carFlow,
                        timeIntervalId
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

    public CarFlow updateCarFlow(String id, int carFlowPm, String timeIntervalId) {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()) {
            return null;
        }

        CarFlow carFlowToUpdate = carFlow.get();
        carFlowToUpdate.setCarFlow(carFlowPm);
        carFlowToUpdate.setTimeIntervalId(timeIntervalId);

        carFlowRepository.save(carFlowToUpdate);

        return carFlowToUpdate;
    }

    public CarFlowRepository getCarFlowRepository() {
        return carFlowRepository;
    }
}
