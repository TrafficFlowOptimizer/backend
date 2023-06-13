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

    public Optimization addOptimization(String crossroadId, int version, String timeIntervalId, List<List<Integer>> results) {
        return optimizationRepository.insert(new Optimization(crossroadId, version, timeIntervalId, results));
    }

    public Optimization deleteOptimizationById(String id) throws Exception {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if(optimization.isEmpty()) {
            throw new Exception("Cannot delete optimization with id: " + id + " because it does not exist.");
        }
        optimizationRepository.deleteById(id);

        return optimization.get();
    }

    public Optimization updateOptimization(String id, String crossroadId, int version, String timeIntervalId, List<List<Integer>> results) throws Exception {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if(optimization.isEmpty()) {
            throw new Exception("Cannot update optimization with id: " + id + " because it does not exist.");
        }
        Optimization optimizationToUpdate = optimization.get();

        optimizationToUpdate.setCrossroadId(crossroadId);
        optimizationToUpdate.setVersion(version);
        optimizationToUpdate.setTimeIntervalId(timeIntervalId);
        optimizationToUpdate.setResults(results);

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

    public int getFreeVersionNumber(String crossroadId) {
        Iterable<Optimization> optimizations = getOptimizationByCrossroadId(crossroadId);
        int max = -1;
        for(Optimization optimization : optimizations) {
            int version = optimization.getVersion();
            if(version > max) {
                max = version;
            }
        }
        return max+1;
    }
}
