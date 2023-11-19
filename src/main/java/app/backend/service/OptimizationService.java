package app.backend.service;

import app.backend.document.Optimization;
import app.backend.repository.OptimizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class OptimizationService {

    private final OptimizationRepository optimizationRepository;

    @Autowired
    public OptimizationService(OptimizationRepository optimizationRepository) {
        this.optimizationRepository = optimizationRepository;
    }

    public Optimization getOptimizationById(String id) {
        return optimizationRepository
                .findById(id)
                .orElse(null);
    }

    public Optimization addOptimization(String crossroadId, int version, String startTimeId, List<List<Integer>> results) {
        return optimizationRepository.insert(
                new Optimization(
                        crossroadId,
                        version,
                        startTimeId,
                        results
                )
        );
    }

    public Optimization addOptimization(String crossroadId, String startTimeId, List<List<Integer>> results) {
        int version = getFreeVersionNumber(crossroadId);
        return optimizationRepository.insert(
                new Optimization(
                        crossroadId,
                        version,
                        startTimeId,
                        results
                )
        );
    }

    public Optimization deleteOptimizationById(String id) {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if (optimization.isEmpty()) {
            return null;
        }

        optimizationRepository.deleteById(id);
        return optimization.get();
    }

    public Optimization updateOptimization(String id, String crossroadId, int version, String startTimeId, List<List<Integer>> results) {
        Optional<Optimization> optimization = optimizationRepository.findById(id);
        if (optimization.isEmpty()) {
            return null;
        }

        Optimization optimizationToUpdate = optimization.get();
        optimizationToUpdate.setCrossroadId(crossroadId);
        optimizationToUpdate.setVersion(version);
        optimizationToUpdate.setStartTimeId(startTimeId);
        optimizationToUpdate.setResults(results);

        optimizationRepository.save(optimizationToUpdate);

        return optimizationToUpdate;
    }

    private List<Optimization> getOptimizationsByCrossroadId(String crossroadId) {
        Iterable<Optimization> optimizations = optimizationRepository.findAllByCrossroadId(crossroadId);

        return StreamSupport
                .stream(optimizations.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Iterable<Optimization> getOptimizationsByCrossroadIdAndStartTime(String crossroadId, String startTimeID) {
        return StreamSupport
                .stream(optimizationRepository.findAllByCrossroadId(crossroadId).spliterator(), false)
                .filter(optimization -> Objects.equals(optimization.getStartTimeId(), startTimeID))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public int getFreeVersionNumber(String crossroadId) {
        Iterable<Optimization> optimizations = getOptimizationsByCrossroadId(crossroadId);

        Optional<Integer> maxVersion = StreamSupport
                .stream(optimizations.spliterator(), false)
                .map(Optimization::getVersion)
                .max(Integer::compareTo);

        return maxVersion.orElse(-1) + 1;
    }

    public Optimization getNewestOptimizationByCrossroadId(String crossroadId, String startTimeID) {
        Iterable<Optimization> optimizations = getOptimizationsByCrossroadIdAndStartTime(crossroadId, startTimeID);

        List<Optimization> sorted = StreamSupport
                .stream(optimizations.spliterator(), false)
                .sorted(Comparator.comparingInt(Optimization::getVersion))
                .toList();

        return sorted.get(sorted.size() - 1);
    }

    public Optimization getSecondNewestOptimizationByCrossroadId(String crossroadId, String startTimeID) {
        Iterable<Optimization> optimizations = getOptimizationsByCrossroadIdAndStartTime(crossroadId, startTimeID);

        List<Optimization> sorted = StreamSupport
                .stream(optimizations.spliterator(), false)
                .sorted(Comparator.comparingInt(Optimization::getVersion))
                .toList();

        if (sorted.size() > 1) {
            return sorted.get(sorted.size() - 2);
        }
        return sorted.get(sorted.size() - 1);
    }

    public OptimizationRepository getOptimizationRepository() {
        return optimizationRepository;
    }
}
