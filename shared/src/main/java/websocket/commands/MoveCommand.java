package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MoveCommand extends UserGameCommand{
    private ChessMove move;
    private ChessGame.TeamColor teamColor;

    public MoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, ChessGame.TeamColor teamColor) {
        super(commandType, authToken, gameID);
        this.move = move;
        this.teamColor = teamColor;
    }

    public ChessMove getMove() {
        return move;
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }
}
