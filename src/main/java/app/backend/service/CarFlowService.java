package app.backend.service;

import app.backend.document.CarFlow;
import app.backend.repository.CarFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarFlowService {
    @Autowired
    CarFlowRepository carFlowRepository;

    public CarFlow getCarFlowById(String id) throws Exception {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()){
            throw new Exception("Cannot get carFlow with id: " + id + " because it does not exist.");
        }

        return carFlow.get();
    }

    public CarFlow addCarFlow(int carFlow, String timeInterval){
        return carFlowRepository.insert(new CarFlow(carFlow, timeInterval));
    }

    public CarFlow deleteCarFlowById(String id) throws Exception {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()) {
            throw new Exception("Cannot delete carFlow with id: " + id + " because it does not exist.");
        }
        carFlowRepository.deleteById(id);
        return carFlow.get();
    }

    public CarFlow updateCarFlow(String id, int carFlowPm, String timeIntervalId) throws Exception {
        Optional<CarFlow> carFlow = carFlowRepository.findById(id);
        if (carFlow.isEmpty()){
            throw new Exception("Cannot update carFlow with id: " + id + " because it does not exist.");
        }
        CarFlow carFlowToUpdate = carFlow.get();

        carFlowToUpdate.setCarFlow(carFlowPm);
        carFlowToUpdate.setTimeIntervalId(timeIntervalId);

        carFlowRepository.save(carFlowToUpdate);

        return carFlowToUpdate;
    }
}
