package app.backend.service;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import app.backend.repository.TrafficLightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrafficLightService {

    private final TrafficLightRepository trafficLightRepository;

    @Autowired
    public TrafficLightService(TrafficLightRepository trafficLightRepository) {
        this.trafficLightRepository = trafficLightRepository;
    }

    public TrafficLight getTrafficLightById(String id) { // TODO: error handling albo integralność przy dodawaniu
        return trafficLightRepository
                .findById(id)
                .orElse(null);
    }

    public TrafficLight addTrafficLight(int index, TrafficLightType type) {
        return trafficLightRepository.insert(
                new TrafficLight(
                        index,
                        type
                )
        );
    }

    public TrafficLight deleteTrafficLightById(String id) {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            return null;
        }

        trafficLightRepository.deleteById(id);
        return trafficLight.get();
    }

    public TrafficLight updateTrafficLight(String id, int index, TrafficLightType type) {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            return null;
        }

        TrafficLight trafficLightToUpdate = trafficLight.get();
        trafficLightToUpdate.setIndex(index);
        trafficLightToUpdate.setType(type);

        trafficLightRepository.save(trafficLightToUpdate);

        return trafficLightToUpdate;
    }

    public TrafficLightRepository getTrafficLightRepository() {
        return trafficLightRepository;
    }
}
