package com.company;

import java.util.ArrayList;

public class ChatServer {

    //Array list to store the user threads
    private ArrayList<UserThread> usersThreadPool;

    public ChatServer() {
        this.usersThreadPool = new ArrayList<>();
    }

    public boolean isUsernameTaken(String username) {
        for(UserThread ut : getUsersThreadPool()) {
            if(!ut.isLogged()) {
                continue;
            }
            if(ut.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<UserThread> getUsersThreadPool() {
        return this.usersThreadPool;
    }

    public synchronized void pushToUsersThreadPool(UserThread uThread) {
        getUsersThreadPool().add(uThread);
    }

    public void newUserAlert(String username){
        for(UserThread ut : getUsersThreadPool()){
            if(!ut.isLogged() || ut.getUsername().equals(username)) {
                continue;
            }
            ut.sendToClient(username + " is now online");
        }
    }

    public void userOfflineAlert(String username){
        for(UserThread ut : getUsersThreadPool()){
            if(!ut.isLogged()) {
                continue;
            }
            ut.sendToClient(username + " is now offline");
        }
    }

    public synchronized void userDisconnected(UserThread ut) {
        getUsersThreadPool().removeIf(element -> element.equals(ut));
        if(ut.isLogged()) {
            System.out.println(ut.getUsername() + " has logged out!");
            userOfflineAlert(ut.getUsername());
        }
    }

    public synchronized void sendPrivateMessage(UserThread ut, String username, String message) {
        if(ut.isLogged()) {
            if(ut.getUsername().equals(username)) {
                ut.sendToClient("You cannot send a PM to yourself.");
                return;
            }
            for(UserThread user : getUsersThreadPool()){
                if(user.getUsername().equals(username)) {
                    user.sendUserMessageToClient("[PRIVATE] " + ut.getUsername(), message);
                    ut.sendToClient("PM to " + username + " sent successfully.");
                    return;
                }
            }
            ut.sendToClient("The user you're trying to send a PM to doesn't exist.");
        }
    }

    public synchronized void sendPublicMessage(UserThread ut, String message) {
        if(ut.isLogged()) {
            if(getUsersThreadPool().size() == 1) {
                ut.sendToClient("You are the only user in the chat room and you cannot send a message to yourself.");
                return;
            }
            for(UserThread user : getUsersThreadPool()){
                if(user.isLogged()) {
                    user.sendUserMessageToClient(ut.getUsername(), message);
                }
            }
        }
    }
}
