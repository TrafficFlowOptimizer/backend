package app.backend.service;

import app.backend.document.Optimization;
import app.backend.repository.OptimizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optimization addOptimization(String crossroadId, String timeIntervalId, List<List<Integer>> results) {
        int version = getFreeVersionNumber(crossroadId);
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

    public List<Optimization> getOptimizationByCrossroadId(String id) {
        Iterable<Optimization> optimizations = optimizationRepository.findAllByCrossroadId(id);
        return StreamSupport.stream(optimizations.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Iterable<Optimization> getOptimizationsByCrossroadIdAndTimeInterval(String crossroadId, String timeIntervalID) {
        Iterable<Optimization> optimizations = optimizationRepository.findAllByCrossroadId(crossroadId);
        List<Optimization> optimizationsFound = new LinkedList<>();
        for(Optimization optimization : optimizations) {
            if(Objects.equals(optimization.getTimeIntervalId(), timeIntervalID)) {
                optimizationsFound.add(optimization);
            }
        }
        return optimizationsFound;
    }

    public int getFreeVersionNumber(String crossroadId) {
        Iterable<Optimization> optimizations = getOptimizationByCrossroadId(crossroadId);

        Optional<Integer> maxVersion = StreamSupport.stream(optimizations.spliterator(), false)
                .map(Optimization::getVersion)
                .max(Integer::compareTo);

        return maxVersion.orElse(-1) + 1;
    }

    public Optimization getNewestOptimizationByCrossroadId(String crossroadId, String timeIntervalID) {
        Iterable<Optimization> optimizations = getOptimizationsByCrossroadIdAndTimeInterval(crossroadId, timeIntervalID);

        List<Optimization> sorted = StreamSupport.stream(optimizations.spliterator(), false).sorted(Comparator.comparingInt(Optimization::getVersion)).toList();

        return sorted.get(sorted.size()-1);
    }

    public Optimization getSecondNewestOptimizationByCrossroadId(String crossroadId, String timeIntervalID) {
        Iterable<Optimization> optimizations = getOptimizationsByCrossroadIdAndTimeInterval(crossroadId, timeIntervalID);

        List<Optimization> sorted = StreamSupport.stream(optimizations.spliterator(), false).sorted(Comparator.comparingInt(Optimization::getVersion)).toList();

        if(sorted.size()>1)
            return sorted.get(sorted.size()-2);
        return sorted.get(sorted.size()-1);
    }
}
