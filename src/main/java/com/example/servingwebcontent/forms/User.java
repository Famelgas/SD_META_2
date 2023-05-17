package com.example.servingwebcontent.forms;

import java.rmi.RemoteException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.HashMap;
import java.util.Scanner;

public class User {
    private String username, password;
    public HashMap<String, String> users;
    public Model model;

    public User() {
        this.username = "";
        this.password = "";
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) throws RemoteException {
        this.username = username;
    }

    public void setPassword(String password) throws RemoteException {
        this.password = password;
    }

    public HashMap<String, String> getUsers() {
        return this.users;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        model.addAttribute("username", this.username);
        model.addAttribute("password", this.password);
        return "register and loged in";
    }
}
