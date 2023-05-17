/**
 * @file IndexStorageBarrel.java - ficheiro que contém a classe IndexStorageBarrel, que corresponde 
 * ao servidor central da aplicação.
 * A class IndexStorageBarrel é responsável pela comunicação com o downloader para receber as informações dos URLs indexados,
 * pelo processamento da informação recebida e pela comunicação com o search module para devolver as repostas ao cliente.
 */

package com.sdproject.Googole;

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

/**
 * @class IndexStorageBarrel class - responsável pela escrita e leitura dos
 *        ficheiros dos ficheiros de objetos,
 *        comunicação com o downloader por multicast para receber as informações
 *        dos URLs indexados,
 *        comunicação com o downloader por TCP para receber o número de threads
 *        de webcrawlers ativos,
 *        comunicação com o search module para devolver as repostas ao cliente e
 *        processamento da informação contida
 *        nos ficheiros de objetos.
 * @param BARRREL_THREAD_POOL_SIZE - número de threads de barrel
 * @param active_downloaders       - número de webcrawlers ativos
 * @param active_barrels           - número de barrels ativos
 */
public class IndexStorageBarrel extends UnicastRemoteObject implements RMIStorageBarrel {
	public static final int BARREL_THREAD_POOL_SIZE = 10;
	public static ArrayList<ArrayList<String>> top_ten_words = new ArrayList<ArrayList<String>>();
	public static int active_downloaders;
	static int active_barrels;

	/**
	 * @class Barrel - classe que corre como thread e recebe por TCP do downloader
	 *        o número de threads de webcrawlers ativos
	 * @param port - porta TCP
	 */
	private static class TCP implements Runnable {
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

	/**
	 * @constructor IndexStorageBarrel - construtor da classe IndexStorageBarrel
	 */
	public IndexStorageBarrel() throws RemoteException {
		super();
	}

	/**
	 * @method admin_page - método que devolve a informação do número de webcrawlers
	 *         ativos,
	 *         número de barrels ativos e top 10 palavras mais pesquisadas
	 * @return admin_info - string com o número de webcrawlers ativos, número de
	 *         barrels ativos e
	 *         top 10 palavras mais pesquisadas caso não haja erros, caso contrário
	 *         devolve a exception
	 */
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

	/**
	 * @method linked_pages - método que recebe um URL e devolve os URLs que lhe
	 *         estão associados
	 * @param s - URL
	 * @return search_answer - string com os URLs que lhe estão associados caso não
	 *         haja erros,
	 *         caso contrário devolve a exception
	 */
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

	/**
	 * @method search - método que recebe as palavras pesquisadas pelo cliente e
	 *         devolve os URLs associados às palavras.
	 *         Além disso, atualiza sempre que há uma pesquisa nova o número de
	 *         vezes que cada palavra foi pesquisada
	 * @param search - palavras pesquisadas pelo cliente
	 * @return search_answer - string com os URLs que estão associados à pesquisa
	 *         caso não haja erros,
	 *         caso contrário devolve a exception
	 */
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

	/**
	 * @method search_barrel - método que recebe as palavras pesquisadas pelo
	 *         cliente
	 *         pesquisa nos ficheiros e devolve os URLs associados às palavras
	 * @param search - palavras pesquisadas pelo cliente
	 * @return array_file - array com os URLs que estão associados à pesquisa
	 * @throws IOException
	 */
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

	/**
	 * @method main - método que inicia o servidor. Cria uma instância da classe
	 *         privada Barrel
	 *         para que esta crie as threads de barrel que recebem a informação do
	 *         downloader,
	 *         cria a thread que faz a comunicação TCP, no porto 2222, para receber
	 *         o número de webcrawlers ativo,
	 *         cria o registo para comunicação RMI, com nome RMIStorageBarrel e no
	 *         porto 2500, para receber a informação
	 *         do cliente através do search module e cria o ficheiro
	 *         TopTenWords.obj, que guarda todas as palavras
	 *         já pesquisadas e o número de vezes que cada uma foi pesquisada.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			IndexStorageBarrel barrel = new IndexStorageBarrel();

			Barrel barrel_thread = new Barrel();
			barrel_thread.start_barrels();

			int port = 2222;
			TCP tcpThread = new TCP(port);
			Thread thread = new Thread(tcpThread);
			thread.start();

			LocateRegistry.createRegistry(2500).rebind("RMIStorageBarrel", barrel);
			System.out.println("Hello Server ready.");

			System.out.println("Server: started and listening for connections...");

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

	/**
	 * @class Barrel - classe que cria as threads de barrel para receber a
	 *        informação dos URLs do downloader.
	 * @param byteStream - byte stream para passagem por multicast.
	 * @param objStream  - object stream para passagem por multicast.
	 * @param socket     - socetk multicast
	 * @param group      - grupo multicast
	 */
	private static class Barrel {
		public ByteArrayOutputStream byteStream;
		public Document doc;
		public MulticastSocket socket;
		public InetAddress group;

		/**
		 * @constructor Barrel - construtor da classe Barrel. Inicia as variáveis.
		 * @throws Exception
		 */
		public Barrel() throws Exception {
			byteStream = new ByteArrayOutputStream();
			new ObjectOutputStream(byteStream);
			group = InetAddress.getByName("239.255.255.1");
		}

		/**
		 * @method start_barrels - método que cria as threads de barrel para receber a
		 *         informação dos URLs do downloader.
		 * @throws Exception
		 */
		public void start_barrels() throws Exception {

			ExecutorService executor = Executors.newFixedThreadPool(5);
			for (int i = 1; i <= 5; i++) {
				Barrels runnable = new Barrels(socket, group, doc, i);
				executor.execute(runnable);
			}
		}

		/**
		 * @class Barrels - thread de barrel para receber a informação dos URLs do
		 *        downloader.
		 * @param socket                 - socket multicast
		 * @param group                  - grupo do multicast
		 * @param URL_Info               - ficheiro que guarda a informação dos URLs,
		 *                               URL, título e citação
		 * @param Words                  - ficheiro que guarda as palavras e os URLs que
		 *                               lhe estão associados
		 * @param URL_With_Links         - ficheiro que guarda os URLs e os URLs que lhe
		 *                               estão associados
		 * @param doc                    - informação recebida do downloader por
		 *                               multicast
		 * @param fileOut_URL_With_Links - file objeto que permite escrever no ficheiro
		 *                               URL_With_Links.obj
		 * @param objOut_URL_With_Links  - object objeto que permite escrever no
		 *                               ficheiro URL_With_Links.obj
		 * @param fileIn_URL_With_Links  - file objeto que permite ler do ficheiro
		 *                               URL_With_Links.obj
		 * @param objIn_URL_With_Links   - object objeto que permite ler do ficheiro
		 *                               URL_With_Links.obj
		 * @param fileOut_URL_Info       - file objeto que permite escrever no ficheiro
		 *                               URL_Info.obj
		 * @param objOut_URL_Info        - object objeto que permite escrever no
		 *                               ficheiro URL_Info.obj
		 * @param fileIn_URL_Info        - file objeto que permite ler do ficheiro
		 *                               URL_Info.obj
		 * @param objIn_URL_Info         - object objeto que permite ler do ficheiro
		 *                               URL_Info.obj
		 * @param fileOut_Words          - file objeto que permite escrever no ficheiro
		 *                               Words.obj
		 * @param objOut_Words           - object objeto que permite escrever no
		 *                               ficheiro Words.obj
		 * @param fileIn_Words           - file objeto que permite ler do ficheiro
		 *                               Words.obj
		 * @param objIn_Words            - object objeto que permite ler do ficheiro
		 *                               Words.obj
		 * 
		 */
		private class Barrels implements Runnable {
			private MulticastSocket socket;
			private InetAddress group;
			public File URL_Info;
			public File Words;
			public File URL_With_Links;
			private Document doc;

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

			/**
			 * @constructor Barrels - construtor da classe Barrels. Inicia as variáveis e
			 *              cria os ficheiros de objetos,
			 *              caso não existam.
			 * @param socket - socket multicast
			 * @param group  - grupo do multicast
			 * @param doc    - informação recebida do downloader por multicast
			 * @param index  - índice da thread barrel
			 * @throws IOException
			 */
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

			/**
			 * @method run - método que executa a thread barrel e atualiza a variavel
			 *         active_barrels.
			 */
			public void run() {
				try {
					ReciveMulticast();
					active_barrels++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/**
			 * @method ReciveMulticast - método que recebe a informação do downloader por
			 *         multicast
			 *         e atualiza cada ficheiro associado à thread com a informação
			 *         recebida.
			 * @throws IOException
			 */
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
}
