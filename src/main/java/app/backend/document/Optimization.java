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

    // TODO: dodac tutaj crossroadId czy w crossroads przechowywać listę wyników?

    private List<List<Integer>> sequences;

    public Optimization(List<List<Integer>> sequences) {
        this.sequences = sequences;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<Integer>> getSequences() {
        return sequences;
    }

    public void setSequences(List<List<Integer>> sequences) {
        this.sequences = sequences;
    }
}
