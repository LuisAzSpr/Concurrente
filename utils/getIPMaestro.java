package org.example.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class getIPMaestro {

    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("IP de la máquina: " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
