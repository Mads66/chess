package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChessBoard {

    private static final int BOARD_SIZE_IN_SQUARES = 8;

    public static final String WHITE_KING = " k ";
    public static final String WHITE_QUEEN = " q ";
    public static final String WHITE_BISHOP = " b ";
    public static final String WHITE_KNIGHT = " n ";
    public static final String WHITE_ROOK = " r ";
    public static final String WHITE_PAWN = " p ";
    public static final String BLACK_KING = " K ";
    public static final String BLACK_QUEEN = " Q ";
    public static final String BLACK_BISHOP = " B ";
    public static final String BLACK_KNIGHT = " N ";
    public static final String BLACK_ROOK = " R ";
    public static final String BLACK_PAWN = " P ";
    public static final String EMPTY = " \u2003 ";

    public static void main(chess.ChessBoard myBoard, List<ChessPosition> highlight) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        System.out.println("White's perspective:");
        drawBoard(myBoard, out, true, highlight); // White's perspective

        System.out.println("\nBlack's perspective:");
        drawBoard(myBoard, out, false, highlight); // Black's perspective
    }

    private static void drawBoard(chess.ChessBoard myBoard, PrintStream out, boolean isWhitePerspective, List<ChessPosition> highlight) {
        String[][] board = initializeBoard(myBoard, isWhitePerspective);

        // Create a 2D boolean array for highlighting
        boolean[][] highlightedPositions = new boolean[BOARD_SIZE_IN_SQUARES][BOARD_SIZE_IN_SQUARES];
        if (highlight != null) {
            for (ChessPosition position : highlight) {
                int row = position.getRow() - 1; // Convert to 0-based index
                int col = position.getColumn() - 1; // Convert to 0-based index
                highlightedPositions[row][col] = true;
            }
        }

        // Print column headers
        out.print("   ");
        if (isWhitePerspective) {
            for (char col = 'a'; col <= 'h'; col++) {
                out.print(" " + col + " ");
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--) {
                out.print(" " + col + " ");
            }
        }
        out.println();

        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            // For white perspective, count rows from 1 up; for black, count from 8 down
            int rowNumber = isWhitePerspective ? (BOARD_SIZE_IN_SQUARES - row) : (row + 1);

            // Print row number on the left side
            out.print(rowNumber + " ");

            int displayRow = isWhitePerspective ? (BOARD_SIZE_IN_SQUARES - row - 1) : row;

            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
                int displayCol = isWhitePerspective ? col : (BOARD_SIZE_IN_SQUARES - col - 1);

                // Check if the position is highlighted
                if (highlightedPositions[displayRow][displayCol]) {
                    setHighlight(out); // Use a special color for highlighted squares
                } else if ((displayRow + displayCol) % 2 == 0) {
                    setBlack(out);
                } else {
                    setWhite(out);
                }

                // Print piece with color based on whether it's black or white
                out.print(board[displayRow][displayCol]);
                resetColor(out);
            }

            // Print row number on the right side
            out.print(" " + rowNumber);
            out.println();
        }

        // Print column headers again at the bottom
        out.print("   ");
        if (isWhitePerspective) {
            for (char col = 'a'; col <= 'h'; col++) {
                out.print(" " + col + " ");
            }
        } else {
            for (char col = 'h'; col >= 'a'; col--) {
                out.print(" " + col + " ");
            }
        }
        out.println();
    }

    private static String[][] initializeBoard(chess.ChessBoard board, boolean isWhitePerspective) {
        String[][] consoleBoard = new String[BOARD_SIZE_IN_SQUARES][BOARD_SIZE_IN_SQUARES];

        for (int row = 0; row < BOARD_SIZE_IN_SQUARES; row++) {
            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
                int displayRow = isWhitePerspective ? row : (BOARD_SIZE_IN_SQUARES - row-1);
                int displayCol = isWhitePerspective ? col : (BOARD_SIZE_IN_SQUARES - col-1);

                ChessPosition position = new ChessPosition(displayRow+1,displayCol+1);
                ChessPiece piece = board.getPiece(position);
                consoleBoard[displayRow][displayCol] = getPieceSymbol(piece);
            }
        }

        return consoleBoard;
    }

    private static String getPieceSymbol(ChessPiece piece) {
        if (piece == null) {return EMPTY;}
        switch (piece.getPieceType()) {
            case KING: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
            default: return EMPTY;
        }
    }

    private static void setWhite(PrintStream out) {
        out.print("\u001B[47m\u001B[30m"); // White background, black text
    }

    private static void setBlack(PrintStream out) {
        out.print("\u001B[100m\u001B[37m"); // Dark grey background, white text
    }

    private static void resetColor(PrintStream out) {
        out.print("\u001B[0m"); // Reset color
    }

    private static void setHighlight(PrintStream out) {
        out.print("\u001B[43m\u001B[30m"); // Yellow background, black text
    }
}

