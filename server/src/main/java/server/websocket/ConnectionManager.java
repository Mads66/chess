package server.websocket;


import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, List<Connection>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, Session session, String auth) {
        if (connections.containsKey(gameID)) {
            var sessions = connections.get(gameID);
            var connection = new Connection(gameID, session, auth);
            sessions.add(connection);
        } else{
            var sessions = new ArrayList<Connection>();
            var connection = new Connection(gameID, session, auth);
            sessions.add(connection);
        connections.put(gameID, sessions);
        }
    }

    public void remove(Integer gameID, String auth) {
        List<Connection> sessions = connections.get(gameID);
        for (Connection c : sessions){
            if (c.getPlayerAuth().equals(auth)){
                sessions.remove(c);
            }
        }
    }

    public void broadcast(Integer gameID, ServerMessage notification, String auth) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameID)) {
            if (c.getSession().isOpen()) {
                if (!(c.getPlayerAuth().equals(auth))) {
                    c.send(new Gson().toJson(notification));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.get(gameID).remove(c);
        }
    }

    public void localBroadcast(Integer gameID, ServerMessage notification, Session session) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameID)) {
            if (c.getSession().isOpen()) {
                if (c.getSession().equals(session)) {
                    c.send(new Gson().toJson(notification));
                }
            } else {
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.get(gameID).remove(c);
        }
    }

    public void generalBroadcast(Integer gameID, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameID)) {
            if (c.getSession().isOpen()) {
                c.send(new Gson().toJson(notification));
            } else {
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.get(gameID).remove(c);
        }
    }

    public List<Connection> getConnections(Integer gameID) {
        return connections.get(gameID);
    }
}
