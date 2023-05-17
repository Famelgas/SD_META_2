package com.example.servingwebcontent.forms;
import java.rmi.*;

public interface RMISearchModule extends Remote {
    public String send_search(String s) throws RemoteException;
    public String send_URL(String s) throws RemoteException;
    public String see_admin_page() throws RemoteException;
    public String see_linked_pages(String s) throws RemoteException;
    public int login(String username, String password) throws RemoteException;
    public int register(String username, String password) throws RemoteException;
}