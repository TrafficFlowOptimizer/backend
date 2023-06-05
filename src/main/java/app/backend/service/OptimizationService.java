package app.backend.service;

import app.backend.document.Optimization;
import app.backend.repository.OptimizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class OptimizationService {
    @Autowired
    OptimizationRepository optimizationRepository;

    public Optimization getOptimizationById(String id) throws Exception {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if(optimization.isEmpty()) {
            throw new Exception("Cannot get optimization with id: " + id + " because it does not exist.");
        }
        return optimization.get();
    }

    public Optimization addOptimization(String crossroadId, int version, List<List<Integer>> sequences) {
        return optimizationRepository.insert(new Optimization(crossroadId, version, sequences));
    }

    public Optimization deleteOptimizationById(String id) throws Exception {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if(optimization.isEmpty()) {
            throw new Exception("Cannot delete optimization with id: " + id + " because it does not exist.");
        }
        optimizationRepository.deleteById(id);

        return optimization.get();
    }

    public Optimization updateOptimization(String id, String crossroadId, int version, List<List<Integer>> sequences) throws Exception {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if(optimization.isEmpty()) {
            throw new Exception("Cannot update optimization with id: " + id + " because it does not exist.");
        }
        Optimization optimizationToUpdate = optimization.get();

        optimizationToUpdate.setCrossroadId(crossroadId);
        optimizationToUpdate.setVersion(version);
        optimizationToUpdate.setSequences(sequences);

        optimizationRepository.save(optimizationToUpdate);

        return optimizationToUpdate;
    }

    public Iterable<Optimization> getOptimizationByCrossroadId(String id) {
        Iterable<Optimization> optimizations = optimizationRepository.findAllByCrossroadId(id);
        List<Optimization> optimizationsFound = new LinkedList<>();
        for(Optimization optimization : optimizations) {
            optimizationsFound.add(optimization);
        }
        return optimizationsFound;
    }
}
