package model;

public record ListGameResponse(int gameID, String whiteUsername, String blackUsername, String gameName) {
    @Override
    public String toString() {
        return "{" +
                "gameID=" + gameID +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", blackUsername='" + blackUsername + '\'' +
                ", gameName='" + gameName + '\'' +
                '}';
    }
}
