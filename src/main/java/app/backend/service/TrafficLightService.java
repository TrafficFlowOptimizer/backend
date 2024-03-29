package app.backend.service;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightDirection;
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

    public TrafficLight getTrafficLightById(String id) {
        return trafficLightRepository
                .findById(id)
                .orElse(null);
    }

    public TrafficLight addTrafficLight(int index, TrafficLightDirection type) {
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

    public TrafficLight updateTrafficLight(String id, int index, TrafficLightDirection type) {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            return null;
        }

        TrafficLight trafficLightToUpdate = trafficLight.get();
        trafficLightToUpdate.setIndex(index);
        trafficLightToUpdate.setDirection(type);

        trafficLightRepository.save(trafficLightToUpdate);

        return trafficLightToUpdate;
    }

    public TrafficLightRepository getTrafficLightRepository() {
        return trafficLightRepository;
    }
}
