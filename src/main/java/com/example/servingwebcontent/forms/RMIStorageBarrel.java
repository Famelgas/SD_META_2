package com.example.servingwebcontent.forms;
import java.rmi.*;

public interface RMIStorageBarrel extends Remote {
    public String search(String search) throws RemoteException;
    public String admin_page() throws RemoteException;
    public String linked_pages(String s) throws RemoteException;
}