package com.example.servingwebcontent.forms;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;

public class IndexStorageBarrel extends UnicastRemoteObject {

    public int BARREL_THREAD_POOL_SIZE;
    public ArrayList<ArrayList<String>> top_ten_words;
    public int active_barrels;
    public int active_downloaders;

    public IndexStorageBarrel() throws RemoteException {
        this.BARREL_THREAD_POOL_SIZE = 10;
        this.top_ten_words = new ArrayList<ArrayList<String>>();
        this.active_barrels = 0;
        this.active_downloaders = 0;
    }

    public int getBARREL_THREAD_POOL_SIZE() {
        return this.BARREL_THREAD_POOL_SIZE;
    }

    public void setBARREL_THREAD_POOL_SIZE(int BARREL_THREAD_POOL_SIZE) {
        this.BARREL_THREAD_POOL_SIZE = BARREL_THREAD_POOL_SIZE;
    }

    public ArrayList<ArrayList<String>> getTop_ten_words() {
        return this.top_ten_words;
    }

    public void setTop_ten_words(ArrayList<ArrayList<String>> top_ten_words) {
        this.top_ten_words = top_ten_words;
    }

    public int getActive_barrels() {
        return this.active_barrels;
    }

    public void setActive_barrels(int active_barrels) {
        this.active_barrels = active_barrels;
    }

    public int getActive_downloaders() {
        return this.active_downloaders;
    }

    public void setActive_downloaders(int active_downloaders) {
        this.active_downloaders = active_downloaders;
    }

    public class TCP implements Runnable {
        private int port;

        /**
         * @constructor TCP - construtor da classe TCP
         * @param port - porta TCP
         */
        public TCP(int port) {
            this.port = port;
        }

        /**
         * @method run - método que corre como thread e recebe por TCP do downloader
         *         o número de threads de webcrawlers ativos
         */
        @Override
        public void run() {
            try {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        active_downloaders = Integer.parseInt(in.readLine());
                    }
                }
            } catch (IOException e) {
                System.out.println("Error starting server in TCP.run: " + e);
            }
        }
    }

    public String linked_pages(String s) throws RemoteException {
        String search_answer = "";
        try {
            Random rand = new Random();
            int index = rand.nextInt(10) + 1;

            File URL_With_Links = new File("URL_With_Links" + Integer.toString(index) + ".obj");
            if (URL_With_Links.exists()) {
                FileWriter URL_With_LinksWriter = new FileWriter(URL_With_Links, true);
                FileReader URL_With_LinksReader = new FileReader(URL_With_Links);

                // Use URL_With_LinksWriter para escrever dados adicionais
                // Use URL_With_LinksReader para ler os dados existentes no arquivo
            } else {
                URL_With_Links.createNewFile();
                FileWriter URL_With_LinksWriter = new FileWriter(URL_With_Links);
                FileReader URL_With_LinksReader = new FileReader(URL_With_Links);
            }

            HashMap<String, String> mapa = new HashMap<String, String>();

            FileOutputStream fileOut = new FileOutputStream(URL_With_Links);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            FileInputStream fileIn = new FileInputStream(URL_With_Links);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);

            ArrayList<String> urls = new ArrayList<String>();
            int aux = 1;

            try {
                while ((mapa = (HashMap<String, String>) objIn.readObject()) != null) {
                    if (mapa.get("URL").equals(s)) {
                        while (mapa.get("Link" + aux) != null) {
                            urls.add(mapa.get("Link" + aux));
                            aux++;
                        }
                    }
                }

                for (int i = 0; i < urls.size(); i++) {
                    search_answer += urls.get(i) + "\n";
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

        } catch (IOException e) {
            return "Exception in IndexStorageBarrel.linked_pages: " + e;
        }

        return search_answer;
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

    public String admin_page() throws RemoteException {
        try {
            Collections.sort(top_ten_words, new Comparator<ArrayList<String>>() {
                @Override
                public int compare(ArrayList<String> list1, ArrayList<String> list2) {
                    String count1 = list1.get(1);
                    String count2 = list2.get(1);
                    return count2.compareTo(count1);
                }
            });
            String top_words = "";
            int count = 0;
            for (ArrayList<String> word : top_ten_words) {
                if (count == 10) {
                    break;
                }
                top_words += word.get(0) + "\n";
            }
            String admin_info = "Active downloaders: " + active_downloaders + "\nActive barrels: " + active_barrels
                    + "\nTop 10 words: \n" + top_words;

            return admin_info;
        } catch (Exception e) {
            return "Error in IndexStorageBarrel.admin_page: " + e;
        }
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

    public class Barrel {
        public ByteArrayOutputStream byteStream;
        public Document doc;
        public MulticastSocket socket;
        public InetAddress group;

        public Barrel() throws Exception {
            byteStream = new ByteArrayOutputStream();
            new ObjectOutputStream(byteStream);
            group = InetAddress.getByName("239.255.255.1");
            socket = null;
            doc = null;
            group = null;
        }

        public ByteArrayOutputStream getByteStream() {
            return this.byteStream;
        }

        public void setByteStream(ByteArrayOutputStream byteStream) {
            this.byteStream = byteStream;
        }

        public Document getDoc() {
            return this.doc;
        }

        public void setDoc(Document doc) {
            this.doc = doc;
        }

        public MulticastSocket getSocket() {
            return this.socket;
        }

        public void setSocket(MulticastSocket socket) {
            this.socket = socket;
        }

        public InetAddress getGroup() {
            return this.group;
        }

        public void setGroup(InetAddress group) {
            this.group = group;
        }

        public void start_barrels() throws Exception {

            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (int i = 1; i <= 5; i++) {
                Barrels runnable = new Barrels(socket, group, doc, i);
                executor.execute(runnable);
            }
        }

        public class Barrels implements Runnable {
            public MulticastSocket socket;
            public InetAddress group;
            public File URL_Info;
            public File Words;
            public File URL_With_Links;
            public Document doc;

            FileOutputStream fileOut_URL_With_Links;
            ObjectOutputStream objOut_URL_With_Links;
            FileInputStream fileIn_URL_With_Links;
            ObjectInputStream objIn_URL_With_Links;

            FileOutputStream fileOut_URL_Info;
            ObjectOutputStream objOut_URL_Info;
            FileInputStream fileIn_URL_Info;
            ObjectInputStream objIn_URL_Info;

            FileOutputStream fileOut_Words;
            ObjectOutputStream objOut_Words;
            FileInputStream fileIn_Words;
            ObjectInputStream objIn_Words;

            public Barrels(MulticastSocket socket, InetAddress group, Document doc, int index) throws IOException {
                this.socket = socket;
                this.group = group;
                this.doc = doc;

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

                File URL_With_Links = new File("URL_With_Links" + Integer.toString(index) + ".obj");
                if (URL_With_Links.exists()) {
                    FileWriter URL_With_LinksWriter = new FileWriter(URL_With_Links, true);
                    FileReader URL_With_LinksReader = new FileReader(URL_With_Links);

                    // Use URL_With_LinksWriter para escrever dados adicionais
                    // Use URL_With_LinksReader para ler os dados existentes no arquivo
                } else {
                    URL_With_Links.createNewFile();
                    FileWriter URL_With_LinksWriter = new FileWriter(URL_With_Links);
                    FileReader URL_With_LinksReader = new FileReader(URL_With_Links);
                }

                this.fileOut_URL_With_Links = new FileOutputStream(URL_With_Links, true); // use true para anexar ao
                                                                                          // arquivo
                this.objOut_URL_With_Links = new ObjectOutputStream(fileOut_URL_With_Links);
                this.fileIn_URL_With_Links = new FileInputStream(URL_With_Links);
                this.objIn_URL_With_Links = new ObjectInputStream(fileIn_URL_With_Links);

                this.fileOut_URL_Info = new FileOutputStream(URL_Info, true); // use true para anexar ao arquivo
                this.objOut_URL_Info = new ObjectOutputStream(fileOut_URL_Info);
                this.fileIn_URL_Info = new FileInputStream(URL_Info);
                this.objIn_URL_Info = new ObjectInputStream(fileIn_URL_Info);

                this.fileOut_Words = new FileOutputStream(Words, true); // use true para anexar ao arquivo
                this.objOut_Words = new ObjectOutputStream(fileOut_Words);
                this.fileIn_Words = new FileInputStream(Words);
                this.objIn_Words = new ObjectInputStream(fileIn_Words);
            }

            public void run() {
                try {
                    ReciveMulticast();
                    active_barrels++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Problemas da meta 1
            public void ReciveMulticast() throws IOException {
                try {
                    socket = new MulticastSocket(1234);
                    socket.joinGroup(group);
                    byte[] buffer = new byte[65507];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);

                    byte[] data = packet.getData();
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);

                    GZIPInputStream gzipIn = new GZIPInputStream(bais);
                    byte[] uncompressedData = gzipIn.readAllBytes();
                    String htmlString = new String(uncompressedData);

                    doc = Jsoup.parse(htmlString);

                    String url = doc.baseUri();
                    System.out.println(url);

                    String titulo = doc.title();

                    StringTokenizer tokens = new StringTokenizer(doc.text(), ".");
                    String citacao = tokens.nextToken() + ".";

                    int aux_aux = 0;

                    ArrayList<ArrayList<String>> array_file = new ArrayList<ArrayList<String>>();
                    ArrayList<String> array_aux = new ArrayList<String>();

                    try (ObjectInputStream objIn_URL_Info = new ObjectInputStream(
                            new FileInputStream("URL_Info.obj"))) {
                        array_file = (ArrayList<ArrayList<String>>) objIn_URL_Info.readObject();
                    } catch (FileNotFoundException e) {
                        // Arquivo não existe, será criado vazio
                    } catch (StreamCorruptedException e) {
                        // Tratar StreamCorruptedException (código inválido)
                        // Provavelmente o arquivo não contém dados válidos ou foi corrompido
                        array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                    } catch (EOFException e) {
                        // Tratar EOFException (fim do arquivo atingido)
                        // O arquivo está vazio, não há dados para serem lidos
                        array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Exception in IndexStorageBarrel.Barrels.ReciveMulticast: " + e);
                    }

                    // Verifica se a URL já existe nos dados lidos
                    boolean urlExists = false;
                    for (ArrayList<String> array : array_file) {
                        if (array.get(0).equals(url)) {
                            urlExists = true;
                            break;
                        }
                    }

                    // Adiciona a URL aos dados se não existir
                    if (!urlExists) {
                        array_aux.add(url);
                        array_aux.add(titulo);
                        array_aux.add(citacao);
                        array_file.add(array_aux);
                    }

                    // Grava os dados atualizados no arquivo URL_Info
                    try (ObjectOutputStream objOut_URL_Info = new ObjectOutputStream(
                            new FileOutputStream("URL_Info.obj"))) {
                        objOut_URL_Info.writeObject(array_file);
                    } catch (IOException e) {
                        System.out.println("Exception in IndexStorageBarrel.Barrels.ReciveMulticast: " + e);
                    }

                    array_file = (ArrayList<ArrayList<String>>) objIn_URL_Info.readObject();
                    System.out.println(array_file);

                    String text = doc.text();
                    String[] words = text.split("\\s+");

                    array_file = new ArrayList<ArrayList<String>>();

                    for (String word : words) {
                        array_aux = new ArrayList<String>();
                        try {
                            try {
                                if (objIn_Words.available() > 0) { // Verificar se há dados disponíveis para leitura
                                    array_file = (ArrayList<ArrayList<String>>) objIn_Words.readObject();
                                } else {
                                    array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                                }
                                // Resto do código...
                            } catch (StreamCorruptedException e) {
                                // Tratar StreamCorruptedException (código inválido)
                                // Provavelmente o arquivo não contém dados válidos ou foi corrompido
                                array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                            } catch (EOFException e) {
                                // Tratar EOFException (fim do arquivo atingido)
                                // O arquivo está vazio, não há dados para serem lidos
                                array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                            } catch (Exception e) {
                                // Tratamento genérico de exceção
                                System.out.println("Exception in IndexStorageBarrel.Barrels.ReciveMulticast: " + e);
                            }
                            for (ArrayList<String> array : array_file) {
                                if (array.get(0).equals(word)) {
                                    array.add(url);
                                    aux_aux = 1;
                                    break;
                                }
                            }
                            if (aux_aux == 0) {
                                array_aux.add(word);
                                array_aux.add(url);
                                array_file.add(array_aux);
                            }
                        } catch (Exception e) {
                            array_aux.add(word);
                            array_aux.add(url);
                            array_file.add(array_aux);
                        }
                        aux_aux = 0;
                    }

                    objOut_Words.writeObject(array_file);

                    aux_aux = 0;

                    Elements links = doc.select("a[href]");

                    array_file = new ArrayList<ArrayList<String>>();

                    for (Element link : links) {
                        array_aux = new ArrayList<String>();
                        try {
                            try {
                                if (objIn_URL_With_Links.available() > 0) { // Verificar se há dados disponíveis para
                                                                            // leitura
                                    array_file = (ArrayList<ArrayList<String>>) objIn_URL_With_Links.readObject();
                                } else {
                                    array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                                }
                                // Resto do código...
                            } catch (StreamCorruptedException e) {
                                // Tratar StreamCorruptedException (código inválido)
                                // Provavelmente o arquivo não contém dados válidos ou foi corrompido
                                array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                            } catch (EOFException e) {
                                // Tratar EOFException (fim do arquivo atingido)
                                // O arquivo está vazio, não há dados para serem lidos
                                array_file = new ArrayList<>(); // Criar um novo ArrayList vazio
                            } catch (Exception e) {
                                // Tratamento genérico de exceção
                                System.out.println("Exception in IndexStorageBarrel.Barrels.ReciveMulticast: " + e);
                            }
                            for (ArrayList<String> array : array_file) {
                                if (array.get(0).equals(link.attr("href"))) {
                                    array.add(url);
                                    aux_aux = 1;
                                    break;
                                }
                            }
                            if (aux_aux == 0) {
                                array_aux.add(link.attr("href"));
                                array_aux.add(url);
                                array_file.add(array_aux);
                            }
                        } catch (Exception e) {
                            array_aux.add(link.attr("href"));
                            array_aux.add(url);
                            array_file.add(array_aux);
                        }

                        objOut_URL_With_Links.writeObject(array_file);

                        aux_aux = 0;

                        active_barrels--;

                        ReciveMulticast();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public void main(String args[]) {
        try {
            IndexStorageBarrel barrel = new IndexStorageBarrel();

            Barrel barrel_thread = new Barrel();
            barrel_thread.start_barrels();

            File t = new File("TopTenWords.obj");
            if (t.createNewFile() == false) {
                FileInputStream tp_words = new FileInputStream("TopTenWords.obj");
                ObjectInputStream tw = new ObjectInputStream(tp_words);
                top_ten_words = (ArrayList<ArrayList<String>>) tw.readObject();
                tw.close();
                tp_words.close();
            } else {
                top_ten_words = new ArrayList<ArrayList<String>>();
            }

        } catch (Exception e) {
            System.out.println("Exception in IndexStorageBarrel.main: " + e);
        }

    }
}
