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

public class LinkedPages {

    public RMIStorageBarrel barrel;


    public LinkedPages() {
        this.barrel = null;
    }


    public RMIStorageBarrel getBarrel() {
        return this.barrel;
    }

    public void setBarrel(RMIStorageBarrel barrel) {
        this.barrel = barrel;
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
}
