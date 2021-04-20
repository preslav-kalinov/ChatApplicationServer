package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        // server is listening on port 8040
        ServerSocket serverSocket = new ServerSocket(8040);
        Socket sock;
        System.out.println("Server started...");
        // instantiating the ChatServer class, which stores and maintains server logic
        ChatServer chatServer = new ChatServer();
        // running infinite loop for getting client request
        while(true) {
            sock = serverSocket.accept();
            clientHandling(sock, chatServer);
        }
    }

    private static void clientHandling(Socket socket, ChatServer chatServer){
        try {
            System.out.println("New client request received: " + socket);
            // pass to the user thread the ChatServer class
            UserThread thread = new UserThread(socket, chatServer);
            thread.start();
            chatServer.pushToUsersThreadPool(thread);
            System.out.println ("Client has been successfully added to the thread pool");
        } catch (IOException ioe) {
            System.out.println("Connection error for socket " + socket);
        }
    }
}
