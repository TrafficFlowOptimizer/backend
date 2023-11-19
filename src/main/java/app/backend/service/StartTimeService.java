package app.backend.service;

import app.backend.document.time.Day;
import app.backend.document.time.StartTime;
import app.backend.document.time.Hour;
import app.backend.repository.StartTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public StartTime addStartTime(Day day, Hour hour) {
        return startTimeRepository.insert(
                new StartTime(
                        day,
                        hour
                )
        );
    }

    public String getStartTimeIdByDayTime(Day day, Hour hour) {
        return startTimeRepository.findAll()
                .stream()
                .filter(stime -> stime.getDay() == day && stime.getHour() == hour)
                .map(StartTime::getId).findFirst()
                .orElse(null);
    }

    public void createStartTimeEnum(){
        if(startTimeRepository.findAll().size()==0) {
            for (Day day : Day.values()) {
                for (Hour hour : Hour.values()) {
                    addStartTime(day, hour);
                }
            }
        }
    }
}
