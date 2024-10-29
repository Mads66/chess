package model;

public record AuthData(String authToken, String username) {

    public AuthData setId(String authToken) {
        return new AuthData(authToken, this.username);
    }
}
