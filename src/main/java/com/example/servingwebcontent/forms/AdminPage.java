package com.example.servingwebcontent.forms;

import java.rmi.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AdminPage {
    public RMIStorageBarrel barrel;
    public int active_downloaders;
    public int active_barrels;
    public ArrayList<ArrayList<String>> top_ten_words;

    public AdminPage() {
        this.barrel = null;
        this.top_ten_words = new ArrayList<ArrayList<String>>();
        this.active_downloaders = 0;
        this.active_barrels = 0;
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
}
