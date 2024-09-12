package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    //could be written as a record
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (type == PieceType.BISHOP) moves = bishopMoves(board, myPosition);
        if(type == PieceType.KING) moves = kingMoves(board, myPosition);
        return moves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[] rowDirections = {1, -1, -1, 1};
        int[] colDirections = {1, 1, -1, -1};

        for (int i = 0; i < rowDirections.length; i++) {
            int rowIncrement = rowDirections[i];
            int colIncrement = colDirections[i];
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += rowIncrement;
                col += colIncrement;

                if (isValidPosition(row, col)) {
                    ChessPosition position = new ChessPosition(row, col);
                    if (board.getPiece(position) == null) {
                        moves.add(new ChessMove(myPosition, position, null));
                    } else if (board.getPiece(position).getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition, position, null));
                        break;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return moves;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[] rowDirections = {-1,-1,-1,0,0,1,1,1}; //down, down-right, down-left, left, right, up, up-right, up-left
        int[] colDirections = {0,1,-1,1,-1,0,1,-1};

        for (int i = 0; i < rowDirections.length; i++){
            int rowIncrement = rowDirections[i];
            int colIncrement = colDirections[i];

            ChessPosition position = new ChessPosition(row+rowIncrement, col+colIncrement);
            if (isValidPosition(row+rowIncrement, col+colIncrement)) {
                if (board.getPiece(position) == null) {
                    moves.add(new ChessMove(myPosition, position, null));
                } else if (board.getPiece(position).getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(myPosition, position, null));
                }
            }
        }
        return moves;
    }

}

