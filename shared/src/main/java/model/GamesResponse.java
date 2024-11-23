package model;

import java.util.Collection;

public class GamesResponse {
    Collection<ListGameResponse> games;

    public Collection<ListGameResponse> getGames() {
        return games;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("games:\n");
        for (ListGameResponse game : games) {
            result.append(game.gameID())
                    .append(": ")
                    .append(game)
                    .append("\n");;
        }
        return result.toString();
    }
}
