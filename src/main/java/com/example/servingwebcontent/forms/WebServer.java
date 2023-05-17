package com.example.servingwebcontent.forms;

import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.sdproject.Googole.RMIStorageBarrel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import org.springframework.ui.Model;

@SpringBootApplication
public class WebServer{

    public AdminPage admin_page = new AdminPage();
    public Downloader downloader = new Downloader();
    public LinkedPages linkedPages = new LinkedPages();
    public SearchModule searchModule = new SearchModule();
    public User user = new User();
    public RMISearchModule s_module;

    @GetMapping("/admin_page")
    public String see_admin_page(Model model) throws RemoteException {
        String server_answer = admin_page.admin_page();
        return server_answer;
    }

    @GetMapping("/index_url")
    public void Downloade(Model model) throws IOException {
        new Downloader();
        String url = "";
        model.addAttribute("url", url);
        downloader.Indexer(url);
    }

    @GetMapping("/linked_pages")
    public void see_linked_pages(Model model) throws RemoteException {
        String str = "";
        model.addAttribute("url", str);
    }

    @GetMapping("/search")
    public void send_search(Model model) throws RemoteException {
        String str = "";
        model.addAttribute("url", str);
    }

    @GetMapping("/login")
    public void login(Model model) throws RemoteException {
        user.login();
    }
    
    
    public WebServer() throws RemoteException {
        super();
    }
    
    public void main(String[] args) {
        try {
            s_module = (RMISearchModule) LocateRegistry.getRegistry(2600).lookup("RMISearchModule");
        }
        catch (Exception e) {
            System.out.println("RMI connection the Search Module failed: " + e);
        }
        SpringApplication.run(WebServer.class, args);
    }
}
