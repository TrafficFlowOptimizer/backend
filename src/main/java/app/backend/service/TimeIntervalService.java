package app.backend.service;

import app.backend.document.TimeInterval;
import app.backend.repository.TimeIntervalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class TimeIntervalService {

    private final TimeIntervalRepository timeIntervalRepository;

    @Autowired
    public TimeIntervalService(TimeIntervalRepository timeIntervalRepository) {
        this.timeIntervalRepository = timeIntervalRepository;
    }

    public TimeInterval getTimeIntervalById(String id) {
        return timeIntervalRepository
                .findById(id)
                .orElse(null);
    }

    public TimeInterval addTimeInterval(LocalTime startTime, LocalTime endTime) {
        return timeIntervalRepository.insert(
                new TimeInterval(
                        startTime,
                        endTime
                )
        );
    }

    public TimeInterval deleteTimeIntervalById(String id) {
        Optional<TimeInterval> timeInterval = timeIntervalRepository.findById(id);
        if (timeInterval.isEmpty()) {
            return null;
        }

        timeIntervalRepository.deleteById(id);
        return timeInterval.get();
    }

    public TimeInterval updateTimeInterval(String id, LocalTime startTime, LocalTime endTime) {
        Optional<TimeInterval> timeInterval = timeIntervalRepository.findById(id);
        if (timeInterval.isEmpty()){
            return null;
        }

        TimeInterval timeIntervalToUpdate = timeInterval.get();
        timeIntervalToUpdate.setStartTime(startTime);
        timeIntervalToUpdate.setEndTime(endTime);

        timeIntervalRepository.save(timeIntervalToUpdate);

        return timeIntervalToUpdate;
    }
}
