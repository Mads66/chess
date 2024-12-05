package websocket.messages;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.List;

public class GameMessage extends ServerMessage {
    ChessGame game;
    List<ChessPosition> highlight;

    public GameMessage(ServerMessageType type, ChessGame game, List<ChessPosition> highlight) {
        super(type);
        this.game = game;
        this.highlight = highlight;
    }

    public ChessGame getGame() {
        return game;
    }
    public List<ChessPosition> getHighlight() {
        return highlight;
    }
}
