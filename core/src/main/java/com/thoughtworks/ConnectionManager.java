package com.thoughtworks;

import com.sun.jdi.InternalException;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    private static Map<String, Connection> connections = new HashMap<String, Connection>();

    public static Map<String, Connection> getConnections() {
        return connections;
    }

    public static void setConnections(Map<String, Connection> connections) {
        ConnectionManager.connections = connections;
    }

    public static void attach(String dbName, Connection connection) {
        if (connections.get(dbName) != null) {
            throw new InternalException("The database for" + dbName +"is connected, connection still remains on thread :" + connections.get(dbName));
        }
        System.out.println("Attaching connection...");
        connections.put(dbName, connection);
        System.out.println("Opened connection:" + connection + " named: " + dbName + "on thread:" + Thread.currentThread());
        //TODO: replace system.out.print with LOG format
    }

    public static void detach(String dbName) {
        System.out.println("Detached connection: " + dbName);
        connections.remove(dbName);
    }
}
