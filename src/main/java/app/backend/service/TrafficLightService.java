package app.backend.service;

import app.backend.document.TrafficLight;
import app.backend.repository.TrafficLightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrafficLightService {
    @Autowired
    TrafficLightRepository trafficLightRepository;

    public TrafficLight getTrafficLightById(String id) throws Exception {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()){
            throw new Exception("Cannot get trafficLight with id: " + id + " because it does not exist.");
        }

        return trafficLight.get();
    }

    public TrafficLight addTrafficLight(int index){
        return trafficLightRepository.insert(new TrafficLight(index));
    }

    public TrafficLight deleteTrafficLightById(String id) throws Exception {
        Optional<TrafficLight> trafficLight = trafficLightRepository.findById(id);
        if (trafficLight.isEmpty()) {
            throw new Exception("Cannot delete trafficLight with id: " + id + " because it does not exist.");
        }
        trafficLightRepository.deleteById(id);
        return trafficLight.get();
    }
}
