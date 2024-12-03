package server.websocket;



import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    private int gameID;
    private Session session;
    private String playerAuth;

    public Connection(int gameID, Session session, String playerAuth) {
        this.gameID = gameID;
        this.session = session;
        this.playerAuth = playerAuth;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }

    public Session getSession() {
        return session;
    }

    public int getGameID() {
        return gameID;
    }
    public String getPlayerAuth() {
        return playerAuth;
    }
}
