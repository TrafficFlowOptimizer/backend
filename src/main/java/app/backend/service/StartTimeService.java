package app.backend.service;

import app.backend.document.time.Day;
import app.backend.document.time.Hour;
import app.backend.document.time.StartTime;
import app.backend.repository.StartTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StartTimeService {

    private final StartTimeRepository startTimeRepository;

    @Autowired
    public StartTimeService(StartTimeRepository startTimeRepository) {
        this.startTimeRepository = startTimeRepository;

        if (startTimeRepository.findAll().isEmpty()) { // if empty instance of DB, then prepopulate
            for (Day day : Day.values()) {
                for (Hour hour : Hour.values()) {
                    startTimeRepository.insert(
                            new StartTime(
                                    day,
                                    hour
                            )
                    );
                }
            }
        }
    }

    public StartTime getStartTimeById(String id) {
        return startTimeRepository
                .findById(id)
                .orElse(null);
    }

    public StartTime getStartTimeByDayTime(Day day, Hour hour) {
        return startTimeRepository.findAll()
                .stream()
                .filter(stime -> stime.getDay() == day && stime.getHour() == hour)
                .findFirst()
                .orElse(null);
    }

// chyba do kosza
//    public StartTime addStartTime(Day day, Hour hour) {
//        return startTimeRepository.insert(
//                new StartTime(
//                        day,
//                        hour
//                )
//        );
//    }

    public String getStartTimeIdByDayTime(Day day, Hour hour) {
        return startTimeRepository.findAll()
                .stream()
                .filter(stime -> stime.getDay() == day && stime.getHour() == hour)
                .map(StartTime::getId).findFirst()
                .orElse(null);
    }
}
