/**
 * @file Downloader.java - ficheiro que contém a classe Downloader, responsável por descarregar a informação das páginas web.
 * A class Downloader é responsável por fazer Web Crawling, e enviar a informação ao index storage barrel atravéz de Multicast. 
 */

package com.sdproject.Googole;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
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

/**
 * @class Downloader class - responsável por indexar os URLs e descarregar a sua
 *        informação. Esta funcionalidade
 *        ocorre em contínuo, parando apenas caso não haja mais URLs na fila
 *        para indexar. Além disso, envia também, por multicasr,
 *        a informação das páginas web para o index storage barrel, mais
 *        especificamente, para as threads barrel.
 * @param downloader      - instância da classe Downloader.
 * @param multicastSocket - socket multicast.
 * @param executorService - serviço/gestor de execução de threads.
 * @param urls            - fila de URLs a indexar.
 * @param enderecoGrupo   - endereço do grupo multicast.
 * @param porta           - porta do grupo multicast.
 * @param byteStream      - byte stream para passagem por multicast.
 * @param objStream       - object stream para passagem por multicast.
 * @param doc             - informação descarregada da página web.
 */
public class Downloader {
    public static Downloader downloader;
    public static MulticastSocket multicastSocket;
    public static ExecutorService executorService;
    public static Queue<String> urls;
    public static InetAddress enderecoGrupo;
    public static int porta;
    public static ByteArrayOutputStream byteStream;
    public static ObjectOutputStream objStream;
    public static Document doc;

    /**
     * @constructor Downloader - construtor da classe Downloader.
     * @throws IOException
     */
    public Downloader() throws IOException {
        /**
         * @param executorService - serviço/gestor de execução de threads mantem-se 5
         *                        threads ativas.
         */
        executorService = Executors.newFixedThreadPool(5);
        urls = new LinkedList<>();

        enderecoGrupo = InetAddress.getByName("239.255.255.1");
        porta = Integer.parseInt("1234");
    }

    /**
     * @method downloadPages - método responsável por criar as threads de web
     *         crawler e iniciar o Web Crawling contínuo.
     *         Chama também o método send_URL, para enviar o número de threads de
     *         web crawler para o index storage barrel, obtem os URLs a indexar a
     *         partir
     *         de uma fila para fazer um index invertido usa-se uma pilha.
     * @throws RemoteException
     */
    public static void downloadPages() throws RemoteException {
        int count_threads = 0;
        ArrayList<WebCrawler> tasks = new ArrayList<>();

        Stack<String> pilha = new Stack<>();

        while (!urls.isEmpty()) {
            pilha.push(urls.remove());
        }

        while (!pilha.isEmpty()) {
            tasks.add(new WebCrawler(pilha.pop()));
        }

        /**
         * Inicia as threads de web crawler com os URLs acociados.
         */
        for (WebCrawler task : tasks) {
            executorService.execute(task);
            send_URL(Integer.toString(count_threads++));
        }
    }

    /**
     * @method shutdown - método responsável por terminar as threads de web crawler
     *         e fechar a conexão multicast.
     * @throws RemoteException
     */
    public static void shutdown() throws RemoteException {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, null);
            send_URL(Integer.toString(0));
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for tasks to finish: " + e.getMessage());
        }
        multicastSocket.close();
    }

    /**
     * @method send_URL - método responsável por enviar o número de threads de web
     *         crawler para o
     *         index storage barrel por TCP, na porta 2222.
     * @param s - número de threads de web crawler.
     * @throws RemoteException
     */
    public static void send_URL(String s) throws RemoteException {
        try {
            Socket socket = new Socket("localhost", 2222);

            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(s.getBytes());

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @class WebCrawler - thread web crawler responsável por fazer Web Crawling, e
     *        enviar toda a
     *        informação ao index storage barrel por multicast.
     * @param URL - URL a indexar.
     */
    private static class WebCrawler implements Runnable {
        private String URL;

        /**
         * @constructor WebCrawler - construtor da classe WebCrawler.
         * @param URL
         */
        public WebCrawler(String URL) {
            this.URL = URL;
        }

        /**
         * @method run - método responsável por fazer Web Crawling, e enviar toda a
         *         informação ao index storage barrel por multicast, vai se converter o
         *         Docoment
         *         obetido com o Jsoup em string, compactala e enviala atravez de
         *         multicast.
         */
        public void run() {
            try {
                doc = Jsoup.connect(URL).get();

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

    /**
     * @method Indexer - método responsável por fazer Web Crawling para o URL
     *         recebido,
     *         enviar toda a informação ao index storage barrel por multicast e
     *         chamar o método downloadPages,
     *         que cria as threads web crawler e inicia o Web Crawling contínuo.
     * @param URL - URL a indexar.
     * @throws IOException
     */
    public static void Indexer(String URL) throws IOException {
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

    /**
     * @class TCP - classe responsável por receber o URL a indexar do search module.
     * @param port - porta TCP.
     */
    private static class TCP implements Runnable {
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

    /**
     * @method main - responsável criar a instância do Downloader e por iniciar a
     *         thread TCP para receção do URL.
     * @param args - argumentos da função main (não são utilizados).
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new Downloader();

        int port = 1111;
        TCP tcpThread = new TCP(port);
        Thread thread = new Thread(tcpThread);
        thread.start();
    }
}