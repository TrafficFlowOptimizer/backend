package app.backend.document.time;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "starttimes")
public class StartTime {
    @Id
    private String id;

    private Day day;

    private Hour hour;

    public StartTime(Day day, Hour hour) {
        this.day = day;
        this.hour = hour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Hour getTime() {
        return hour;
    }

    public void setTime(Hour hour) {
        this.hour = hour;
    }
}
