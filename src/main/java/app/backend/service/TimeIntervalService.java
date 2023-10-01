package app.backend.service;

import app.backend.document.CarFlow;
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

    public TimeInterval getTimeIntervalById(String id) throws Exception {
        Optional<TimeInterval> timeInterval = timeIntervalRepository.findById(id);
        if (timeInterval.isEmpty()){
            throw new Exception("Cannot get timeInterval with id: " + id + " because it does not exist.");
        }

        return timeInterval.get();
    }

    public TimeInterval addTimeInterval(LocalTime startTime, LocalTime endTime){
        return timeIntervalRepository.insert(new TimeInterval(startTime, endTime));
    }

    public TimeInterval deleteTimeIntervalById(String id) throws Exception {
        Optional<TimeInterval> timeInterval = timeIntervalRepository.findById(id);
        if (timeInterval.isEmpty()) {
            throw new Exception("Cannot delete timeInterval with id: " + id + " because it does not exist.");
        }
        timeIntervalRepository.deleteById(id);
        return timeInterval.get();
    }

    public TimeInterval updateTimeInterval(String id, LocalTime startTime, LocalTime endTime) throws Exception {
        Optional<TimeInterval> timeInterval = timeIntervalRepository.findById(id);
        if (timeInterval.isEmpty()){
            throw new Exception("Cannot update timeInterval with id: " + id + " because it does not exist.");
        }
        TimeInterval timeIntervalToUpdate = timeInterval.get();

        timeIntervalToUpdate.setStartTime(startTime);
        timeIntervalToUpdate.setEndTime(endTime);

        timeIntervalRepository.save(timeIntervalToUpdate);

        return timeIntervalToUpdate;
    }
}
