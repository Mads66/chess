package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadGameMessage extends ServerMessage {
    GameData game;

    public LoadGameMessage(ServerMessageType type, GameData game) {
        super(type);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }

}
