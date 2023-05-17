/**
 * @file RMIStorageBarrel.java - ficheiro que contém a interface RMIStorageBarrel, para comunicação
 * RMI entre o search module e o index storage barrel.
 */

package com.sdproject.Googole;
import java.rmi.*;

/**
 * @interface RMIStorageBarrel interface - responsável pela comunicação RMI entre o search module e o index storage barrel.
 */
public interface RMIStorageBarrel extends Remote {
	public String search(String search) throws RemoteException;
	public String admin_page() throws RemoteException;
	public String linked_pages(String s) throws RemoteException;
}