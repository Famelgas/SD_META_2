package com.example.servingwebcontent.forms;

/**
 * @file Client.java - ficheiro que contém a classe Client, responsável pela interface do utilizador e pela conexão RMI ao search module.
 * Através da class Client o cliente consegue conectar-se por RMI ao search module e realizar as operações de pesquisa que deseja.
 */

import java.rmi.*;
import java.rmi.registry.*;
import java.util.Scanner;

/**
 * @class Client class - responsável pela interface do utilizador e pela ligação
 *        RMI com o search module.
 *        Nesta class encontramos apenas a função main() responsável por
 *        estabelecer a ligação RMI e apresentar ao cliente o menu
 *        de pesquisa da aplicação, podendo este escolher opções para realizar
 *        as operações que deseja.
 */
public class Client {
    /**
     * @constructor Client constructor
     */
    public Client() throws RemoteException {
        /**
         * Calls the super class constructor
         */
        super();
    }

    /**
     * @method main function - função responsável pela ligação RMI com o search
     *         module e pela apresentação do menu de pesquisa.
     *         A ligaçao RMI com o search module é feita, através do método
     *         lookup(), à referência remota RMISearchModule, que está a correr na
     *         porta 2600.
     *         O menu é apresentado ao utilizador através de um ciclo while, que
     *         termina quando o utilizador escolhe a opção de sair.
     *         O ciclo apresenta as opções a cliente e espera pelo seu input e chama
     *         as respetivas funções no search module,
     *         apresentando as respostas ao utilizador quando estas são devolvidas.
     *         Caso o utilizador não tenha o registo e o login efetuado, tem a opção
     *         de introduzir um URL, de pesquisar palavras no Googol,
     *         efetuar login, efetuar registo ou aceder à página de administração.
     *         Caso o utilizador tenha o registo e o login efetuado, tem a opção de
     *         introduzir um URL, de pesquisar palavras no Googol,
     *         consultar a lista de páginas com ligação para uma página específica
     *         introduzida através de um URL ou aceder à página de administração.
     * @param args - argumentos da função main (não são utilizados).
     */
    public static void main(String[] args) {
        /**
         * Buffer to store the user input
         * Option to store the user option
         * Server answer to store the server answer
         * Username to store the username
         * Password to store the password
         * Log to check if the user is logged in
         * Reg to check if the user is registered
         */
        String buffer;
        int option = 0;
        String server_answer;
        String username;
        String password;
        int log = 0;
        int reg = 0;
        /**
         * Try with resources to close the scanner
         */
        try (Scanner sc = new Scanner(System.in)) {
            /**
             * Try to connect to the server
             */
            RMISearchModule s_module = (RMISearchModule) LocateRegistry.getRegistry(2600).lookup("RMISearchModule");
            /**
             * Print the client sent subscription to server
             */
            System.out.println("Client sent subscription to server");
            while (true) {
                System.out.println(">==================== Googol ====================<");

                if (log == 1) {

                    System.out.println("1 - Introduzir um URL");
                    System.out.println("2 - Pesquisar no Googol");
                    System.out.println("3 - Consultar lista de páginas com ligação para uma página específica");
                    System.out.println("4 - Página de administração");
                    System.out.println("5 - Sair");

                    option = sc.nextInt();
                    sc.nextLine();

                    if (option == 1) {
                        System.out.println("> Insira um URL: ");
                        buffer = sc.nextLine();
                        server_answer = s_module.send_URL(buffer);
                        System.out.println(server_answer);
                    } else if (option == 2) {
                        System.out.println("> Insira a sua pesquisa: ");
                        buffer = sc.nextLine();
                        server_answer = s_module.send_search(buffer);
                        System.out.println(server_answer);
                    } else if (option == 3) {
                        System.out.println("> Insira um URL: ");
                        buffer = sc.nextLine();
                        server_answer = s_module.see_linked_pages(buffer);
                        System.out.println(server_answer);
                    } else if (option == 4) {
                        server_answer = s_module.see_admin_page();
                        System.out.println(server_answer);
                    } else if (option == 5) {
                        break;
                    } else {
                        System.out.println("Opção inválida");
                    }

                } else {
                    System.out.println("1 - Introduzir um URL");
                    System.out.println("2 - Pesquisar no Googol");
                    System.out.println("3 - Página de administração");
                    System.out.println("4 - Efetuar login");
                    System.out.println("5 - Efetuar registo");
                    System.out.println("6 - Sair");

                    option = sc.nextInt();
                    sc.nextLine();
                    if (option == 1) {
                        System.out.println("> Insira um URL: ");
                        buffer = sc.nextLine();
                        server_answer = s_module.send_URL(buffer);
                        System.out.println(server_answer);
                    } else if (option == 2) {
                        System.out.println("> Insira a sua pesquisa: ");
                        buffer = sc.nextLine();
                        server_answer = s_module.send_search(buffer);
                        System.out.println(server_answer);
                    } else if (option == 3) {
                        server_answer = s_module.see_admin_page();
                        System.out.println(server_answer);
                    } else if (option == 4) {
                        System.out.println("Username: ");
                        username = sc.nextLine();
                        System.out.println("Password: ");
                        password = sc.nextLine();

                        while ((log = s_module.login(username, password)) != 1) {
                            System.out.println("Login falhou\nTente novamente");
                            System.out.println("Username: ");
                            username = sc.nextLine();
                            System.out.println("Password: ");
                            password = sc.nextLine();
                        }
                        System.out.println("Login efetuado com sucesso");
                    } else if (option == 5) {
                        System.out.println("Username: ");
                        username = sc.nextLine();
                        System.out.println("Password: ");
                        password = sc.nextLine();

                        while ((reg = s_module.register(username, password)) != 1) {
                            System.out.println("Registo falhou\nTente novamente");
                            System.out.println("Username: ");
                            username = sc.nextLine();
                            System.out.println("Password: ");
                            password = sc.nextLine();
                        }
                        System.out.println("Registo efetuado com sucesso");
                    } else if (option == 6) {
                        break;
                    } else {
                        System.out.println("Opção inválida");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}