package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserThread extends Thread {
    private String username;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket sock;
    private ChatServer chatServer;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]*$";

    public UserThread(Socket sock, ChatServer chatServer) throws IOException {
        this.username = null;
        this.sock = sock;
        this.dis = new DataInputStream (sock.getInputStream());
        this.dos = new DataOutputStream (sock.getOutputStream());
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        sendToClient("Please enter your username: ");
        try {
            while (true) {
                String username = recvFromClient();
                if (checkUser(username)) {
                    userLoginSuccessful(username);
                    break;
                }
            }
            while (true) {
                String recv = recvFromClient();
                sendMessage(recv);
            }
        } catch (IOException ioe) {
            try {
                this.dos.close();
                this.dis.close();

            } catch (IOException e) {
                System.out.println("Failure closing data streams.");
            }
        }
        chatServer.userDisconnected(this);
    }

    private String recvFromClient() throws IOException {
        return this.dis.readUTF();
    }

    public void sendUserMessageToClient(String username, String message) {
        try {
            dos.writeUTF(currentTime() + " " + username + ": " + message);
        } catch (IOException ioe) {
            System.out.println("Cannot send user message to socket " + this.sock + ", reason: " + ioe.getMessage());
        }
    }

    public void sendToClient(String message) {
        try {
            dos.writeUTF("[SERVER] " + message);
        } catch (IOException ioe) {
            System.out.println("Cannot write message to socket " + this.sock + ", reason: " + ioe.getMessage());
        }
    } 

    private boolean checkUser(String username) {
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher m = pattern.matcher(username);
        boolean match = m.matches();
        if (match) {
            if(chatServer.isUsernameTaken(username)){
                sendToClient("Username is taken. Write another username.");
                return false;
            }
            else {
                return true;
            }
        }
        sendToClient("Usernames can contain only letters, digits, underscores and dashes. Make sure there are no whitespaces.\nPlease enter your username again:");
        return false;
    }

    private void userLoginSuccessful(String username){
        this.username = username;
        System.out.println(username + " has logged in!");
        sendToClient("Greetings " + username + "! You've entered in the chat room. To private message someone, write as shown: \n/pm username message. \nTo write to everyone, just send your message. \nTo exit, write \"/quit\".");
        printAllActiveUsers();
        chatServer.newUserAlert(username);
    }

    public void printAllActiveUsers() {
        ArrayList<String> usernames = new ArrayList<>();
        for(UserThread user : getChatServer().getUsersThreadPool()){
            if(!user.isLogged()) {
                continue;
            }
            usernames.add(user.getUsername());
        }
        sendToClient("List of active users:\n" + String.join("\n", usernames));
    }

    private void sendMessage(String msg) {
        ArrayList<String> messageArray = new ArrayList<>(Arrays.asList(msg.split(" ")));
        if(messageArray.get(0).equals("/pm") && messageArray.size() > 2) {
            //we have a private message
            List<String> actualMessage = messageArray.subList(2, messageArray.size());
            chatServer.sendPrivateMessage(this, messageArray.get(1), String.join(" ", actualMessage));
            return;
        } else if(messageArray.get(0).equals("/pm")) {
            sendToClient("To send a pm, please use the following format: /pm username message");
            return;
        }
        chatServer.sendPublicMessage(this, msg);
    }

    private String currentTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime time = LocalDateTime.now();
        return dtf.format(time);
    }

    public String getUsername() {
        return username;
    }

    public boolean isLogged() {
        return this.username != null;
    }

    public ChatServer getChatServer() {
        return chatServer;
    }
}
