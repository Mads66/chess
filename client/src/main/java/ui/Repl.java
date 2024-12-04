package ui;

import ui.websocket.NotificationHandler;
import websocket.messages.Notification;

import java.util.Scanner;

import static java.awt.Color.RED;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverURL) {
        client = new ChessClient(serverURL, this);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 chess. Type Help to get started. ♕");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                if (!result.equals("quit") && !result.contains("failure")) {
                    System.out.print(result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        stop(scanner);

    }

    private static void stop(Scanner scanner) {
        scanner.close();
        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }

    @Override
    public void notify(Notification notification) {
        System.out.println(notification.getMessage());
        printPrompt();
    }
}
