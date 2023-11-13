package app.backend.service;

import app.backend.document.time.Day;
import app.backend.document.time.Time;
import app.backend.document.time.StartTime;
import app.backend.repository.StartTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StartTimeService {

    private final StartTimeRepository startTimeRepository;

    @Autowired
    public StartTimeService(StartTimeRepository startTimeRepository) {
        this.startTimeRepository = startTimeRepository;
    }

    public StartTime getStartTimeById(String id) {
        return startTimeRepository
                .findById(id)
                .orElse(null);
    }

    public StartTime addStartTime(Day day, Time time) {
        return startTimeRepository.insert(
                new StartTime(
                        day,
                        time
                )
        );
    }

    public String getStartTimeIdByDayTime(Day day, Time time){
        return startTimeRepository.findAll()
                .stream()
                .filter(stime -> stime.getDay() == day && stime.getTime() == time)
                .map(StartTime::getId).findFirst()
                .orElse(null);
    }

    public StartTime deleteStartTimeById(String id) {
        Optional<StartTime> startTime = startTimeRepository.findById(id);
        if (startTime.isEmpty()) {
            return null;
        }

        startTimeRepository.deleteById(id);
        return startTime.get();
    }

    public StartTime updateStartTime(String id, Day day, Time time) {
        Optional<StartTime> startTime = startTimeRepository.findById(id);
        if (startTime.isEmpty()) {
            return null;
        }

        StartTime startTimeToUpdate = startTime.get();
        startTimeToUpdate.setDay(day);
        startTimeToUpdate.setTime(time);

        startTimeRepository.save(startTimeToUpdate);

        return startTimeToUpdate;
    }
}
