package app.backend.document;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public class Video {

    @Id
    private String id;

    @NotBlank
    private String crossroadId;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotBlank
    private String startTimeId;

    private byte[] data;

    //Video time in seconds
    private final Integer time;

    public Video(String crossroadId, String name, String type, String startTimeId, byte[] data, Integer time) {
        this.crossroadId = crossroadId;
        this.name = name;
        this.type = type;
        this.startTimeId = startTimeId;
        this.data = data;
        this.time = time;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTimeId() {
        return startTimeId;
    }

    public void setStartTimeId(String startTimeId) {
        this.startTimeId = startTimeId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Integer getTime() {
        return time;
    }
}
