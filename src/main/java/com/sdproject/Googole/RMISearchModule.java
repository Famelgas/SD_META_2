/**
 * @file RMISearchModule.java - ficheiro que contém a interface RMISearchModule, 
 * para a comunicação RMI entre o cliente e o search module e o search module e o index storage barrel.
 */

package com.sdproject.Googole;
import java.rmi.*;

/**
 * @interface RMISearchModule interface - responsável pela comunicação RMI entre o cliente e o search module 
 * e o search module e o index storage barrel.
 */
public interface RMISearchModule extends Remote {
    public String send_search(String s) throws RemoteException;
    public String send_URL(String s) throws RemoteException;
    public String see_admin_page() throws RemoteException;
    public String see_linked_pages(String s) throws RemoteException;
    public int login(String username, String password) throws RemoteException;
    public int register(String username, String password) throws RemoteException;
}