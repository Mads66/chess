package model;

public record ListGameResponse(int gameID, String whiteUsername, String blackUsername, String gameName) {
    @Override
    public String toString() {
        return "{" + " Game Name = " + gameName +
                ", White Username = " + whiteUsername +
                ", Black Username = " + blackUsername +
                '}';
    }
}
