package app.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotBlank
    private String crossroadId;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotBlank
    private String timeIntervalId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data;

    public Video() {}

    public Video(String crossroadId, String name, String type, String timeIntervalId, byte[] data) {
        this.crossroadId = crossroadId;
        this.name = name;
        this.type = type;
        this.timeIntervalId = timeIntervalId;
        this.data = data;
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

    public String getTimeIntervalId() {
        return timeIntervalId;
    }

    public void setTimeIntervalId(String timeIntervalId) {
        this.timeIntervalId = timeIntervalId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
