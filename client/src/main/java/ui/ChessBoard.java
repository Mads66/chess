package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChessBoard {

    private static final int BOARD_SIZE_IN_SQUARES = 8;

    public static final String WHITE_KING = " ♔ ";
    public static final String WHITE_QUEEN = " ♕ ";
    public static final String WHITE_BISHOP = " ♗ ";
    public static final String WHITE_KNIGHT = " ♘ ";
    public static final String WHITE_ROOK = " ♖ ";
    public static final String WHITE_PAWN = " ♙ ";
    public static final String BLACK_KING = " ♚ ";
    public static final String BLACK_QUEEN = " ♛ ";
    public static final String BLACK_BISHOP = " ♝ ";
    public static final String BLACK_KNIGHT = " ♞ ";
    public static final String BLACK_ROOK = " ♜ ";
    public static final String BLACK_PAWN = " ♟ ";
    public static final String EMPTY = " \u2003 ";

    private static final String BOLD_TEXT = "\u001B[1m";
    private static final String RESET_COLOR = "\u001B[0m";

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        System.out.println("White's perspective:");
        drawBoard(out, true); // White's perspective

        System.out.println("\nBlack's perspective:");
        drawBoard(out, false); // Black's perspective
    }

    private static void drawBoard(PrintStream out, boolean isWhitePerspective) {
        String[][] board = initializeBoard(isWhitePerspective);

        // Print column headers
        out.print("  ");
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
            int displayRow = isWhitePerspective ? (BOARD_SIZE_IN_SQUARES - row) : (row + 1);

            // Print row number on the left side
            out.print(displayRow + " ");

            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
                int displayCol = isWhitePerspective ? col : (BOARD_SIZE_IN_SQUARES - col - 1);

                // Alternate square colors
                if ((row + displayCol) % 2 == 0) {
                    setWhite(out);
                } else {
                    setBlack(out);
                }

                // Print piece with color based on whether it's black or white
                out.print(getColoredPiece(board[BOARD_SIZE_IN_SQUARES - displayRow][displayCol]));
                resetColor(out);
            }

            // Print row number on the right side
            out.print(" " + displayRow);
            out.println();
        }

        // Print column headers again at the bottom
        out.print("  ");
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

    private static String[][] initializeBoard(boolean isWhitePerspective) {
        String[][] board = new String[BOARD_SIZE_IN_SQUARES][BOARD_SIZE_IN_SQUARES];

        if (isWhitePerspective) {
            // White pieces at the bottom, black pieces at the top
            board[0] = new String[]{BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP,
                    BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};
            board[1] = new String[]{BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN,
                    BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN};

            board[6] = new String[]{WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN,
                    WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN};
            board[7] = new String[]{WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP,
                    WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK};
        } else {
            // Black pieces at the bottom, white pieces at the top
            board[0] = new String[]{WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP,
                    WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK};
            board[1] = new String[]{WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN,
                    WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN};

            board[6] = new String[]{BLACK_PAWN, BLACK_PAWN, BLACK_PAWN,
                    BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN};
            board[7] = new String[]{BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP,
                    BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};
        }

        // Fill empty squares
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; col++) {
                board[row][col] = EMPTY;
            }
        }

        return board;
    }

    private static String getColoredPiece(String piece) {
        if (piece.contains("♔") || piece.contains("♕") || piece.contains("♗") || piece.contains("♘") || piece.contains("♖") || piece.contains("♙")) {
            return BOLD_TEXT + piece + RESET_COLOR;
        } else if (piece.contains("♚") || piece.contains("♛") || piece.contains("♝") || piece.contains("♞") || piece.contains("♜") || piece.contains("♟")) {
            return BOLD_TEXT +  piece + RESET_COLOR;
        } else {
            return piece;
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
}

