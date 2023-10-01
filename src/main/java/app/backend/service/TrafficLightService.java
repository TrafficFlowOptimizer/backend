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
    public TrafficLight getTrafficLightById(String id) throws Exception {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()){
            throw new Exception("Cannot get trafficLight with id: " + id + " because it does not exist.");
        }

        return trafficLight.get();
    }

    public TrafficLight addTrafficLight(int index, String name, TrafficLightType type){
        return trafficLightRepository.insert(new TrafficLight(index, name, type));
    }

    public TrafficLight deleteTrafficLightById(String id) throws Exception {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            throw new Exception("Cannot delete trafficLight with id: " + id + " because it does not exist.");
        }
        trafficLightRepository.deleteById(id);
        return trafficLight.get();
    }

    public TrafficLight updateTrafficLight(String id, int index, String name, TrafficLightType type) throws Exception {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            throw new Exception("Cannot update trafficLight with id: " + id + " because it does not exist.");
        }
        TrafficLight trafficLightToUpdate = trafficLight.get();

        trafficLightToUpdate.setIndex(index);
        trafficLightToUpdate.setName(name);
        trafficLightToUpdate.setType(type);

        trafficLightRepository.save(trafficLightToUpdate);

        return trafficLightToUpdate;
    }

    public TrafficLightRepository getTrafficLightRepository() {
        return trafficLightRepository;
    }
}