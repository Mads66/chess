package model;

public record UserData(String username, String password, String email) {

    public UserData setId(String username) {
        return new UserData(username, this.password, this.email);
    }
}