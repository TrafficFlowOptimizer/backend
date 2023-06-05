package app.backend.document;

import jakarta.validation.constraints.NotBlank;
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

    private List<List<Integer>> sequences;

    public Optimization(String crossroadId, int version, List<List<Integer>> sequences) {
        this.crossroadId = crossroadId;
        this.version = version;
        this.sequences = sequences;
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

    public List<List<Integer>> getSequences() {
        return sequences;
    }

    public void setSequences(List<List<Integer>> sequences) {
        this.sequences = sequences;
    }
}
