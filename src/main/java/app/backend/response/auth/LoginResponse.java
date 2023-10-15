package app.backend.response.auth;

public class LoginResponse {
    private String id;
    private String token;

    public LoginResponse(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public String getEmail() {
        return id;
    }

    public void setEmail(String email) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
