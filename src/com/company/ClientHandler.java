package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class ClientHandler extends Thread {
    private final String clientID;
    private final Socket socket;
    private final ObjectOutputStream writer;
    private final ObjectInputStream reader;
    private String clientName;

    public ClientHandler(Socket socket, String clientID, ObjectOutputStream writer, ObjectInputStream reader) {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        this.clientID = clientID;
    }

    public ObjectOutputStream getWriter() {
        return writer;
    }

    @Override
    public void run() {
        try {
            this.clientName = (String) reader.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        while (true) {
            Object o1 = null;
            Object o2 = null;
            try {
                while ((o1 = reader.readObject()) != null) {
                    //System.out.println("Server reads the message; " + o1);
                    System.out.println("Object 2 is:" + o2);
                    o2 = reader.readUnshared();

                    //o2 = reader.readObject();
                    System.out.println("Server reads object; " + o2.getClass());
                    System.out.println(o2);
                    sendToAllClients(o1,o2);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendToAllClients(Object o1, Object o2) {
        Date date = new Date();
        for (ClientHandler client : ChatServer.getClientThreads()) {
            Object text = String.format("%s [%tb %td, %tT]: %s", this.clientName, date, date, date, (String)o1 + "\n");
            ObjectOutputStream localWriter = client.getWriter();
            try {
                localWriter.writeObject(text);
                localWriter.writeObject(o2);
                localWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
