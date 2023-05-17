package com.example.servingwebcontent.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.ui.Model;

public class SearchModule extends UnicastRemoteObject{

    public RMIStorageBarrel barrel;
    public ArrayList<ArrayList<String>> top_ten_words;
    public HashMap<String, String> users;
    String message;

    public SearchModule() throws RemoteException {
        try {
            this.barrel = (RMIStorageBarrel) LocateRegistry.getRegistry(2500).lookup("RMIStorageBarrel");
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.top_ten_words = new ArrayList<ArrayList<String>>();
        this.users = new HashMap<String, String>();
        this.message = "";
    }

    // Gera getters e setters


    public RMIStorageBarrel getBarrel() {
        return this.barrel;
    }

    public void setBarrel(RMIStorageBarrel barrel) {
        this.barrel = barrel;
    }

    public ArrayList<ArrayList<String>> getTop_ten_words() {
        return this.top_ten_words;
    }

    public void setTop_ten_words(ArrayList<ArrayList<String>> top_ten_words) {
        this.top_ten_words = top_ten_words;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, String> getUsers() {
        return this.users;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    public String send_search(String s) throws RemoteException {
        System.out.println("Search: " + s);
        return barrel.search(s);
    }

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

    public int register(String username, String password) throws RemoteException {
        if (username.equals("user") || password.equals("pass")) {
            return 1;
        }
        return 0;
    }

    public int login(String username, String password) throws RemoteException {
        if (username.equals("user") && password.equals("pass")) {
            return 1;
        } else {
            return 0;
        }
    }

    public String see_admin_page() throws RemoteException {
        String server_answer = barrel.admin_page();
        return server_answer;
    }

    public String see_linked_pages(String s) throws RemoteException {
        return barrel.linked_pages(s);
    }

    public ArrayList<ArrayList<String>> search_barrel(String search) throws IOException {
        ArrayList<String> search_list = new ArrayList<String>();

        Random rand = new Random();
        int index = rand.nextInt(5) + 1;

        // Verifica se o arquivo URL_Info existe
        File URL_Info = new File("URL_Info" + Integer.toString(index) + ".obj");
        if (URL_Info.exists()) {
            // O arquivo existe, abra-o em modo de adição
            FileWriter URL_InfoWriter = new FileWriter(URL_Info, true);
            FileReader URL_InfoReader = new FileReader(URL_Info);

            // Use URL_InfoWriter para escrever dados adicionais
            // Use URL_InfoReader para ler os dados existentes no arquivo
        } else {
            // O arquivo não existe, crie-o vazio
            URL_Info.createNewFile();

            // Use FileWriter para escrever dados no arquivo vazio
            FileWriter URL_InfoWriter = new FileWriter(URL_Info);
            FileReader URL_InfoReader = new FileReader(URL_Info);
        }

        // Repita o mesmo processo para os outros arquivos: Words e URL_With_Links
        File Words = new File("Words" + Integer.toString(index) + ".obj");
        if (Words.exists()) {
            FileWriter WordsWriter = new FileWriter(Words, true);
            FileReader WordsReader = new FileReader(Words);

            // Use WordsWriter para escrever dados adicionais
            // Use WordsReader para ler os dados existentes no arquivo
        } else {
            Words.createNewFile();
            FileWriter WordsWriter = new FileWriter(Words);
            FileReader WordsReader = new FileReader(Words);
        }

        FileInputStream fileIn_Words = new FileInputStream(Words);
        ObjectInputStream objIn_Words = new ObjectInputStream(fileIn_Words);

        FileInputStream fileIn_URL_Info = new FileInputStream(URL_Info);
        ObjectInputStream objIn_URL_Info = new ObjectInputStream(fileIn_URL_Info);

        String[] palavras = search.split("\\s+");

        int aux = 1;

        ArrayList<ArrayList<String>> array_file = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> links = new ArrayList<ArrayList<String>>();

        for (String palavra : palavras) {
            System.out.println(palavra);
            ArrayList<String> links_palavra = new ArrayList<String>();
            try {
                array_file = (ArrayList<ArrayList<String>>) objIn_Words.readObject();
                for (ArrayList<String> array : array_file) {
                    if (array.get(0).equals(palavra)) {
                        for (int i = 1; i < array.size(); i++) {
                            links_palavra.add((String) array.get(i));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }
            links.add(links_palavra);
        }

        HashSet<String> common = new HashSet<>(links.get(0));
        for (ArrayList<String> innerList : links) {
            common.retainAll(innerList);
        }

        ArrayList<String> Urls = new ArrayList<>(common);

        ArrayList<ArrayList<String>> links_show = new ArrayList<ArrayList<String>>();
        ArrayList<String> URL_show = new ArrayList<String>();

        for (String url : Urls) {
            try {
                array_file = (ArrayList<ArrayList<String>>) objIn_URL_Info.readObject();
                for (ArrayList<String> array : array_file) {
                    if (array.get(0).equals(url)) {
                        URL_show.add(url);
                        URL_show.add(array.get(1));
                        URL_show.add(array.get(2));
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }
            links_show.add(URL_show);
        }

        return links_show;
    }

    public String search(String search) throws RemoteException {
        ArrayList<ArrayList<String>> links_show;
        String search_answer = "";

        try {
            links_show = search_barrel(search);

            int count = 0;
            for (ArrayList<String> word : top_ten_words) {
                if (word.get(0).equals(search)) {
                    word.set(1, Integer.toString(Integer.parseInt(word.get(1)) + 1));
                    count++;
                    break;
                }
            }
            if (count == 0) {
                ArrayList<String> word = new ArrayList<String>();
                word.add(search);
                word.add("1");
                top_ten_words.add(word);
            }

            for (int i = 0; i < links_show.size(); i++) {
                search_answer += links_show.get(i).get(0) + "\n" + links_show.get(i).get(1) + "\n"
                        + links_show.get(i).get(2) + "\n\n";
            }
        } catch (IOException e) {
            return "Exception in IndexStorageBarrel.search: " + e;
        }

        return search_answer;

    }

    @GetMapping("/search")
    public void search(Model model) {
        try {
            barrel = (RMIStorageBarrel) LocateRegistry.getRegistry(2500).lookup("RMIStorageBarrel");

            SearchModule search_module = new SearchModule();


            LocateRegistry.createRegistry(2600).rebind("RMISearchModule", search_module);


            model.addAttribute("message", this.message);
            String message = barrel.search(this.message);
            //UL LI
            ModelAndView modelAndView = new ModelAndView("/search");
            modelAndView.addObject(message);
        } catch (Exception e) {
            System.out.println("Search Module failed: " + e);
        }
    }
}
