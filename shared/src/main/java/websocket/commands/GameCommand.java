package websocket.commands;

import chess.ChessPosition;

import java.util.List;

public class GameCommand extends UserGameCommand{
    List<ChessPosition> highlight;

    public GameCommand(CommandType commandType, String authToken, Integer gameID, List<ChessPosition> highlight) {
        super(commandType, authToken, gameID);
        this.highlight = highlight;
    }

    public List<ChessPosition> getHighlight() {
        return highlight;
    }
}
