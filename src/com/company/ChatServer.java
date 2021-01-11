package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    public static final int PORT = 8889;
    private static int clientCounter = 0;
    private static Vector<ClientHandler> clientThreads = new Vector<>();
    private static ServerSocket serverSocket;

    public static Vector<ClientHandler> getClientThreads() {
        return clientThreads;
    }

    public static void main(String[] args) {
        new ChatServer().go();
    }

    public void go() {
        //Create Server
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        //Collect and Handle client requests
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                //Create  a client with its iostreams
                String clientID = String.format("Client_%s", clientCounter);
                System.out.println(String.format("Connection with %s is established ", clientID));
                ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
                Thread t = new ClientHandler(clientSocket, clientID, writer, reader);
                clientThreads.add((ClientHandler) t);
                t.start();
                clientCounter++;
                if(this.clientThreads.size()==0){
                    this.serverSocket.close();
                }
            } catch (Exception e) {
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}

