package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatClient {
    private Socket clientSocket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;

    public ChatClient() {
        try {
            this.clientSocket = new Socket("127.0.0.1", ChatServer.PORT);
            this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
            this.reader = new ObjectInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            try {
                clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void sendMessage(String message,Object object) {
        try {
            writer.writeObject(message);
            writer.writeUnshared(object);
            System.out.println("Client sends message: "+ object);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            writer.writeObject(message);
            System.out.println("Client sends message: "+ message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList readMessage(){
        ArrayList result = new ArrayList();
        Object o1 = null;
        Object o2 = null;
        try {
            while ((o1 = reader.readObject()) != null) {
                System.out.println("Client reads the message; "+(String)o1);
                o2 = reader.readObject();
                System.out.println("Client reads object; "+ o2.getClass());
                System.out.println(o2);
                result.add(o1);
                result.add(o2);
                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
