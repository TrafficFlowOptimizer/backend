package app.backend.document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "optimizations")
public class Optimization {
    @Id
    private String id;

    private String crossroadId;

    @PositiveOrZero
    private int version;

    @NotNull
    private TimeInterval timeIntervalId;

    private List<List<Integer>> results;

    public Optimization(String crossroadId, int version, TimeInterval timeIntervalId, List<List<Integer>> results) {
        this.crossroadId = crossroadId;
        this.version = version;
        this.timeIntervalId = timeIntervalId;
        this.results = results;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrossroadId() {
        return crossroadId;
    }

    public void setCrossroadId(String crossroadId) {
        this.crossroadId = crossroadId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public TimeInterval getTimeIntervalId() {
        return timeIntervalId;
    }

    public void setTimeIntervalId(TimeInterval timeIntervalId) {
        this.timeIntervalId = timeIntervalId;
    }

    public List<List<Integer>> getResults() {
        return results;
    }

    public void setResults(List<List<Integer>> results) {
        this.results = results;
    }
}
