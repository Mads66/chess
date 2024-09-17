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
        if (type == PieceType.KING) moves = kingMoves(board, myPosition);
        if (type == PieceType.KNIGHT) moves = knightMoves(board, myPosition);
        if (type == PieceType.PAWN) moves = pawnMoves(board, myPosition);
        if (type == PieceType.ROOK) moves = rookMoves(board, myPosition);
        if (type == PieceType.QUEEN) moves = queenMoves(board, myPosition);
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
        int[] rowDirections = {-1, -1, -1, 0, 0, 1, 1, 1}; //down, down-right, down-left, left, right, up, up-right, up-left
        int[] colDirections = {0, 1, -1, 1, -1, 0, 1, -1};

        for (int i = 0; i < rowDirections.length; i++) {
            int rowIncrement = rowDirections[i];
            int colIncrement = colDirections[i];
            int row = myPosition.getRow() + rowIncrement;
            int col = myPosition.getColumn() + colIncrement;

            ChessPosition position = new ChessPosition(row, col);
            List<ChessMove> tempMoves = (List<ChessMove>) assembleChessMoves(board, myPosition, position, row, col);
            moves.addAll(tempMoves);
        }
        return moves;
    }

    private Collection<ChessMove> assembleChessMoves(ChessBoard board, ChessPosition myPosition, ChessPosition position, int row, int col) {
        List<ChessMove> moves = new ArrayList<>();
        if (isValidPosition(row, col)) {
            if (board.getPiece(position) == null) {
                moves.add(new ChessMove(myPosition, position, null));
            } else if (board.getPiece(position).getTeamColor() != pieceColor) {
                moves.add(new ChessMove(myPosition, position, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[] rowDirections = {2, 2, 1, 1, -1, -1, -2, -2}; //up-right, up-left, left-up, right-up, left-down, right-down, down-right, down-left
        int[] colDirections = {1, -1, 2, -2, 2, -2, 1, -1};

        for (int i = 0; i < rowDirections.length; i++) {
            int rowIncrement = rowDirections[i];
            int colIncrement = colDirections[i];
            int row = myPosition.getRow() + rowIncrement;
            int col = myPosition.getColumn() + colIncrement;

            ChessPosition position = new ChessPosition(row, col);
            List<ChessMove> tempMoves = (List<ChessMove>) assembleChessMoves(board, myPosition, position, row, col);
            moves.addAll(tempMoves);
        }

        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[] rowDirections = {};
        int[] colDirections = {};
        if (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7){
            rowDirections = new int[] {-1,-2,-1,-1};
            colDirections = new int[] {0,0,-1,1};
        } else if (pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2){
            rowDirections = new int[] {1,2,1,1};
            colDirections = new int[] {0,0,-1,1};
        } else if (pieceColor == ChessGame.TeamColor.BLACK) {
            rowDirections = new int[] {-1,-1,-1};
            colDirections = new int[] {0,-1,1};
        } else {
            rowDirections = new int[] {1,1,1};
            colDirections = new int[] {0,-1,1};
        }

        for (int i = 0; i < rowDirections.length; i++) {
            int rowIncrement = rowDirections[i];
            int colIncrement = colDirections[i];
            int row = myPosition.getRow() + rowIncrement;
            int col = myPosition.getColumn() + colIncrement;
            ChessPosition position = new ChessPosition(row, col);

            if (isValidPosition(row, col)) {
                if (colIncrement == 0 && moves.size() == i && board.getPiece(position) == null) {
                    if (row == 8 || row == 1) {
                        moves.add(new ChessMove(myPosition, position, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, position, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, position, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, position, PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(myPosition, position, null));
                    }
                }
                if (colDirections.length == 3 && i > 0 && board.getPiece(position) != null) {
                    if (board.getPiece(position).getTeamColor() != pieceColor) {
                        if (row == 8 || row == 1) {
                            moves.add(new ChessMove(myPosition, position, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, position, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, position, PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, position, PieceType.ROOK));
                        } else {
                            moves.add(new ChessMove(myPosition, position, null));
                        }
                    }
                }

            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[] rowDirections = {1, -1, 0, 0};
        int[] colDirections = {0, 0, -1, 1};

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

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        List<ChessMove> tempRook = (List<ChessMove>) rookMoves(board, myPosition);
        moves.addAll(tempRook);
        List<ChessMove> tempBishop = (List<ChessMove>) bishopMoves(board, myPosition);
        moves.addAll(tempBishop);
        return moves;
    }
}



