/**
 * @file SearchModule.java - ficheiro que contém a classe SearchModule, responsável pela ligação entre o cliente e o servidor index storage barrel.
 * A class SearchModule conecta-se por RMI tanto ao cliente como ao servidor, realiazando as funções intermédias de ligação entre os dois.
 * Além disso, comunica também com o downloader, enviando-lhe os URLs que o cliente pretende indexar.
 */

package com.sdproject.Googole;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.HashMap;


/**
 * @class SearchModule class - responsável pela ligação entre o cliente e o servidor index storage barrel.
 * Nesta class encontramos várias funções: main(), estabelece as ligações, login(), register(), 
 * send_search(), envia os termos pesquisados pelo cliente ao servidor, 
 * send_URL(), envia os URLs que o cliente pretende indexar ao downloader,
 * see_admin_page(), pede ao index storage barrel a informação da página de administração,
 * see_linked_pages(), envia ao servidor um URL e mostra ao cliente os links que lhe estão associados.
 */
public class SearchModule extends UnicastRemoteObject implements RMISearchModule {
    public static RMIStorageBarrel barrel;
    public HashMap<String, String> users;

    /**
     * @constructor SearchModule constructor
     */
    public SearchModule() throws RemoteException {
        super();
    }

    /**
     * @method login function - função responsável por verificar se o login do cliente é válido.
     * Visto que não era necessário guardar a informação dos clientes optamos por fazer um login 
     * hardcoded, ou seja, apenas aceitamos o login de um cliente com username "user" e password "pass".
     * @param username - username do cliente
     * @param password - password do cliente
     * @return 1 se o login for bem sucedido, 0 caso contrário
     */
    public int login(String username, String password) throws RemoteException {
        if(username.equals("user") && password.equals("pass")) {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * @method register function - função responsável por registar um novo cliente.
     * Visto que não era necessário guardar a informação dos clientes optamos por fazer um registo 
     * hardcoded, ou seja, apenas aceitamos o registo de um cliente com username "user" e password "pass".
     * @param username - username do cliente
     * @param password - password do cliente
     * @return 1 se o registo for bem sucedido
     */
    public int register(String username, String password) throws RemoteException {
        if (username.equals("user") || password.equals("pass")) {
            return 1;
        }
        return 0;
    }

    /**
     * @method send_search function - função responsável por enviar as palavras pesquisadas pelo cliente ao servidor
     * e devolver ao cliente a resposta.
     * @param s - palavras pesquisadas pelo cliente
     * @return - resposta do servidor
     */
    public String send_search(String s) throws RemoteException {
        System.out.println("Search: " + s);
        return barrel.search(s);

    }

    /**
     * @method send_URL function - função responsável por enviar os URLs que o cliente pretende indexar ao downloader
     * @param s - URL que o cliente pretende indexar
     * @return - "URL: " + s + " indexado" caso o URL seja enviado com sucesso, exception caso contrário
     */    
    public String send_URL(String s) throws RemoteException {
        try {
            Socket socket = new Socket("localhost", 1111);

            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(s.getBytes());

            socket.close();
        } catch (IOException e) {
            return "Exception in SearchModule.send_URL: " + e;
        }
        return "URL: " + s + " indexado.";
    }

    /**
     * @method see_admin_page function - função responsável por pedir ao index storage barrel a informação da 
     * página de administração e devolver ao cliente a resposta com a informação.
     * @return - resposta do servidor
     */
    public String see_admin_page() throws RemoteException {
        String server_answer = barrel.admin_page();
        return server_answer;
    }

    /**
     * @method see_linked_pages function - função responsável por enviar ao servidor um URL e devolver ao cliente
     * as páginas associadas a esse URL.
     * @param s - URL que o cliente pretende ver os links associados
     * @return - resposta do servidor com as páginas associadas
     */
    public String see_linked_pages(String s) throws RemoteException {
        return barrel.linked_pages(s);
    }
    
    /**
     * @method main function - função responsável por estabelecer as ligações entre o cliente e o search module
     * e o search moduele e o servidor.
     * A ligação RMI entre o search module e o index storage barrel é feita através do porto 2500 e ao nome RMIStorageBarrel.
     * O registo para conexão RMI entre o search module e o cliente e criada no porto 2600 com nome RMISearchModule.
     * Para que a conexão seja bem sucedida é necessário que o servidor esteja a correr, para tal, primeiro o search module
     * é conectado ao index storage barrel e apensas de seguida e criado o registo para a ligação RMI com o cliente.
     * @param args - argumentos da função main (não são utilizados)
     */
    public static void main(String args[]) {
        try {
            barrel = (RMIStorageBarrel) LocateRegistry.getRegistry(2500).lookup("RMIStorageBarrel");
            
            SearchModule search_module = new SearchModule();

            LocateRegistry.createRegistry(2600).rebind("RMISearchModule", search_module);
            
            System.out.println("Search Module is ready.");
            
        } catch (Exception e) {
            System.out.println("Search Module failed: " + e);
        }
    }
}