package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard myBoard;
    private TeamColor teamTurn;
    private Boolean gameOver;

    public ChessGame() {
        myBoard = new ChessBoard();
        myBoard.resetBoard();
        teamTurn = TeamColor.WHITE;
        gameOver = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(myBoard, chessGame.myBoard) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(myBoard, teamTurn);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);
        return checkMoves(startPosition, piece.getTeamColor(), board);
    }

    private Collection<ChessMove> checkMoves(ChessPosition startPosition, TeamColor teamColor, ChessBoard board) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> tempMoves = piece.pieceMoves(board, startPosition);

        ChessBoard originalBoard = copyBoard(board);
        for (ChessMove move : tempMoves) {
            try {
                moving(move, piece);
            } catch (InvalidMoveException e) {
                continue;
            }
            if (!isInCheck(teamColor)) {
                validMoves.add(move);
            }
            setBoard(copyBoard(originalBoard));
        }
        setBoard(copyBoard(originalBoard));
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = myBoard.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece here");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move: not a piece move");
        }
        moving(move, piece);
        if (piece.getTeamColor() == TeamColor.BLACK) {
            setTeamTurn(TeamColor.WHITE);
        } else {
            setTeamTurn(TeamColor.BLACK);
        }
    }

    private void moving(ChessMove move, ChessPiece piece) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPosition endPosition = move.getEndPosition();
        if (endPosition.getRow() > 8 || endPosition.getRow() < 1 || endPosition.getColumn() > 8 || endPosition.getColumn() < 1) {
            throw new InvalidMoveException("Invalid move: out of bounds");
        }
        if (move.getPromotionPiece() != null) {
            board.addPiece(endPosition, new ChessPiece(teamTurn, move.getPromotionPiece()));
        } else {
            board.addPiece(endPosition, piece);
        }
        board.addPiece(move.getStartPosition(), null);
        setBoard(board);
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = getBoard();
        ChessPosition king = kingPosition(teamColor, board);

        Collection<ChessMove> opponentMoves = checkOpponentMove(teamColor, king, board);
        return !opponentMoves.isEmpty();
    }

    private ChessPosition kingPosition(TeamColor teamColor, ChessBoard board) {
        ChessPosition king = null;
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                        king = new ChessPosition(row, col);
                    }
                }
            }
        }
        return king;
    }

    private Collection<ChessMove> checkOpponentMove(TeamColor teamColor, ChessPosition king, ChessBoard board) {
        Collection<ChessMove> opponentMoves = new ArrayList<>();
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                opponentMoves.addAll(checkMovesOpponent(teamColor, king, board, row, col));
            }
        }
        return opponentMoves;
    }

    private Collection<ChessMove> checkMovesOpponent(TeamColor teamColor, ChessPosition king, ChessBoard board, int row, int col) {
        Collection<ChessMove> opponentMoves = new ArrayList<>();
        ChessPosition opponentPosition = new ChessPosition(row, col);
        ChessPiece piece = myBoard.getPiece(opponentPosition);
        if (piece != null) {
            if (teamColor != piece.getTeamColor()) {
                Collection<ChessMove> moves = piece.pieceMoves(board, opponentPosition);
                for (ChessMove move : moves) {
                    ChessPosition enemy = move.getEndPosition();
                    if (enemy.equals(king)) {
                        opponentMoves.addAll(moves);
                        break;
                    }
                }
            }
        }
        return opponentMoves;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessPosition> teamPieces = getTeamPositions(teamColor);
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        for (ChessPosition piece : teamPieces) {
            possibleMoves.addAll(validMoves(piece));
        }
        if (possibleMoves.isEmpty()){
            gameOver = true;
        }
        return possibleMoves.isEmpty();
    }

    private Collection<ChessPosition> getTeamPositions(TeamColor teamColor) {
        Collection<ChessPosition> teamPieces = new ArrayList<>();
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = myBoard.getPiece(position);
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        teamPieces.add(position);
                    }
                }
            }
        }
        return teamPieces;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        Collection<ChessPosition> teamPieces = getTeamPositions(teamColor);
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        for (ChessPosition piece : teamPieces) {
            possibleMoves.addAll(validMoves(piece));
        }
        if (possibleMoves.isEmpty()){
            gameOver = true;
        }

        return possibleMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        myBoard = board;
    }

    public void resignGame(){
        gameOver = true;
    }

    public Boolean getGameOver(){
        return gameOver;
    }

    private ChessBoard copyBoard(ChessBoard board) {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                newBoard.addPiece(position, piece);
            }
        }
        return newBoard;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return myBoard;
    }
}
