package app.backend.response;

public class VideoResponseMessage {

    private String message;

    public VideoResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
