package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard myBoard = new ChessBoard();
    private TeamColor teamTurn;
    public ChessGame() {

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
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);
        setTeamTurn(piece.getTeamColor());
        if (isInCheck(teamTurn)){
            validMoves = checkMoves(startPosition, teamTurn, board);
        }
        else {
            Collection<ChessMove> tempMoves = piece.pieceMoves(board, startPosition);
            for (move: tempMoves){
                makeMove(move);
                if (!isInCheck(teamTurn)){
                    validMoves.add(move);
                }
            }
        }
        setBoard(board);
        return validMoves;
    }

    private Collection<ChessMove> checkMoves(ChessPosition startPosition, TeamColor teamColor, ChessBoard board) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> tempMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : tempMoves) {
            try {
                makeMove(move);
            } catch (InvalidMoveException e){
                break;
            }

            ChessPosition simKing = kingPosition(teamColor, myBoard);
            Collection<ChessMove> simOpponentMoves = checkOpponentMove(teamColor, simKing, myBoard);
            if (!simOpponentMoves.isEmpty()){
                validMoves.add(move);
            }
        }
        setBoard(board);
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, move.getPromotionPiece()));
        }
        else {board.addPiece(move.getEndPosition(), piece);}
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
                ChessPosition opponentPosition = new ChessPosition(row, col);
                ChessPiece piece = myBoard.getPiece(opponentPosition);
                if (piece != null) {
                    if (teamColor != piece.getTeamColor()) {
                        Collection<ChessMove> moves = piece.pieceMoves(board, opponentPosition);
                        for (ChessMove move : moves) {
                            ChessPosition opponent = move.getEndPosition();
                            if (opponent == king) {
                                opponentMoves.add((ChessMove) moves);
                                break;
                            }
                        }
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                myBoard.addPiece(position, piece);
            }
        }
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
