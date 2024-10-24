package org.example.ClientsNode;
import org.example.Maestro;
import org.example.utils.Libro;

import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private static final String HOST = "127.0.1.1";//"localhost";
    private static final int PORT = 5000;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private static Pattern pattern = Pattern.compile("Libros/([^/]+)\\.txt$");

    private String leerLibro(String ruta){
        StringBuilder libro = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                libro.append(linea).append(" "); // Añadir cada línea al StringBuilder
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return libro.toString();
    }

    private void connectToServer(String libro,String titulo) {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Titulo:"+titulo);
            out.flush();
            out.println("Contenido:"+libro);
            out.flush();
            String inputline;
            while ((inputline = in.readLine()) != null) { // Se lee un mensaje del cliente
                if(inputline.startsWith("EXITO")){
                    System.out.println(inputline);
                    break;
                }
                if(inputline.startsWith("ERROR")){
                    System.out.println(inputline);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String titulo(String ruta){
        Matcher matcher = pattern.matcher(ruta);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "None";
    }

    public static void main(String [] args){
        String ruta = "/home/luis/Documents/Parcial_Concurrente_/Libros/libro1.txt";
        Client client = new Client();
        String titulo = client.titulo(ruta);
        String libro = client.leerLibro(ruta);
        client.connectToServer(libro,titulo);
    }
}
