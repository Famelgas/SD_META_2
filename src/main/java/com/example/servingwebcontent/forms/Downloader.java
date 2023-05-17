package com.example.servingwebcontent.forms;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.ui.Model;

import java.rmi.RemoteException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Stack;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class Downloader implements Serializable {
    public Downloader downloader;
    public MulticastSocket multicastSocket;
    public ExecutorService executorService;
    public Queue<String> urls;
    public InetAddress enderecoGrupo;
    public int porta;
    public ByteArrayOutputStream byteStream;
    public ObjectOutputStream objStream;
    public Document doc;
    public String url;

    public Downloader() {
        this.executorService = Executors.newFixedThreadPool(5);
        this.urls = new LinkedList<>();

        try {
            this.enderecoGrupo = InetAddress.getByName("239.255.255.1");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.porta = Integer.parseInt("1234");

        this.multicastSocket = null;
        this.byteStream = null;
        this.objStream = null;
        this.doc = null;
    }

    //made get and set
    public Downloader getDownloader() {
        return this.downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    public MulticastSocket getMulticastSocket() {
        return this.multicastSocket;
    }

    public void setMulticastSocket(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Queue<String> getUrls() {
        return this.urls;
    }

    public void setUrls(Queue<String> urls) {
        this.urls = urls;
    }

    public InetAddress getEnderecoGrupo() {
        return this.enderecoGrupo;
    }

    public void setEnderecoGrupo(InetAddress enderecoGrupo) {
        this.enderecoGrupo = enderecoGrupo;
    }

    public int getPorta() {
        return this.porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public ByteArrayOutputStream getByteStream() {
        return this.byteStream;
    }

    public void setByteStream(ByteArrayOutputStream byteStream) {
        this.byteStream = byteStream;
    }

    public ObjectOutputStream getObjStream() {
        return this.objStream;
    }

    public void setObjStream(ObjectOutputStream objStream) {
        this.objStream = objStream;
    }

    public Document getDoc() {
        return this.doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public void downloadPages() throws RemoteException {
        int count_threads = 0;
        ArrayList<WebCrawler> tasks = new ArrayList<>();

        Stack<String> stack = new Stack<>();

        while (!urls.isEmpty()) {
            stack.push(urls.remove());
        }

        while (!stack.isEmpty()) {
            tasks.add(new WebCrawler(stack.pop()));
        }

        for (WebCrawler task : tasks) {
            executorService.execute(task);
            send_URL(Integer.toString(count_threads++));
        }
    }

    public void shutdown() throws RemoteException {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, null);
            send_URL(Integer.toString(0));
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for tasks to finish: " + e.getMessage());
        }
        multicastSocket.close();
    }

    public void send_URL(String s) throws RemoteException {
        try {
            Socket socket = new Socket("127.0.0.1", 2222);

            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(s.getBytes());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Indexer(String URL) throws IOException {
        try {
            Document doc = Jsoup.connect(URL).get();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (!urls.contains(link.attr("href")))
                    urls.add(link.attr("href"));
            }

            multicastSocket = new MulticastSocket();
            byteStream = new ByteArrayOutputStream();

            String htmlString = doc.outerHtml();

            GZIPOutputStream gzip_objStream = new GZIPOutputStream(byteStream);
            gzip_objStream.write(htmlString.getBytes());
            gzip_objStream.close();

            byte[] data = htmlString.getBytes();

            data = byteStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, enderecoGrupo, porta);
            multicastSocket.setSendBufferSize(65507);
            multicastSocket.send(packet);

            downloadPages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class WebCrawler implements Runnable {
        private String URL;

        public WebCrawler(String URL) {
            this.URL = URL;
        }

        public void run() {
            try {
                doc = Jsoup.connect(URL).get();

                Document doc = Jsoup.connect(URL).get();

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    if (!urls.contains(link.attr("href")))
                        urls.add(link.attr("href"));
                }

                System.out.println("URL: " + urls);

                multicastSocket = new MulticastSocket();
                byteStream = new ByteArrayOutputStream();

                String htmlString = doc.outerHtml();

                GZIPOutputStream gzip_objStream = new GZIPOutputStream(byteStream);
                gzip_objStream.write(htmlString.getBytes());
                gzip_objStream.close();

                byte[] data = htmlString.getBytes();

                data = byteStream.toByteArray();

                // --------------------------------------------------------------

                DatagramPacket packet = new DatagramPacket(data, data.length, enderecoGrupo, porta);
                multicastSocket.setSendBufferSize(65507);
                multicastSocket.send(packet);

                downloadPages();

            } catch (IOException e) {
                System.out.println("Erro threads Downloader: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class TCP implements Runnable {
        private int port;

        /**
         * @constructor TCP - construtor da classe TCP.
         * @param port - porta TCP.
         */
        public TCP(int port) {
            this.port = port;
        }

        /**
         * @method run - método responsável por receber o URL a indexar do search module
         *         e chamar a
         *         método Indexer e iniciar o Web Crawling.
         */
        public void run() {
            try {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            Indexer(inputLine);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            }
        }
    }

    @GetMapping("/downloader")
    public void Downloade(Model model) throws IOException {
        new Downloader();

        TCP tcpThread = new TCP(1111);
        Thread thread = new Thread(tcpThread);
        thread.start();
    }
    
}