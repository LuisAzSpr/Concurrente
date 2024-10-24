package org.example.ClientsNode;

import org.example.utils.EntradaCliente;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientModelo {
    public Socket socket;
    public final String  HOST = "127.0.1.1";
    public final int PORT = 6000;
    private static Map<Character,Integer> valoresCaracteres = new HashMap<>();


    private double[] connectToServer(String titulo,double[] entradas) {
        try {
            socket = new Socket(HOST, PORT);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // manda objetos
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); // recibe objetos
            EntradaCliente entradaCliente = new EntradaCliente(titulo,entradas);
            oos.writeObject(entradaCliente);
            oos.flush();
            double[] salida = (double[]) ois.readObject(); // Recibe y deserializa el modelo
            System.out.println("Las probabilidades para clase son: ");
            for(int i=0;i<salida.length;i++){
                System.out.print(salida[i]+"\t");
            }
            return salida;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void mapeo(){
        int valor = 0;
        for(char c='a';c<='z';c++){
            valoresCaracteres.put(c,valor++);
        }
        for(char c='0';c<='9';c++){
            valoresCaracteres.put(c,valor++);
        }
    }

    public static double[] obtenerEntradas(String texto){
        double[] valores = new double[6];
        texto = limpiarPalabra(texto);
        int tamanioTexto = texto.length();
        texto = texto.substring(tamanioTexto-6);
        System.out.println(texto);
        for(int i=0;i<valores.length;i++){
            valores[i] = valoresCaracteres.get(texto.charAt(i));
        }
        return valores;
    }

    public static char prediccion(double[] probabilidades){
        int max = 0;
        double probMax = probabilidades[0];
        for(int i=1;i<probabilidades.length;i++){
            if(probabilidades[i]>probMax){
                max = i;
            }
        }

        for(Character c:valoresCaracteres.keySet()){
            if(valoresCaracteres.get(c)==max){
                return c;
            }
        }
        return '-';
    }

    public static String limpiarPalabra(String cadena){
        String cadenaCotenido = cadena.toString().toLowerCase();
        String libroSinTildes = cadenaCotenido
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ","")
                .replaceAll(" ","");
        return libroSinTildes.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static void main(String[]args){
        mapeo();
        ClientModelo clientModelo = new ClientModelo();
        String cadena = "hablamos mas tarde";
        double[] entradasDouble = obtenerEntradas(cadena);
        double[] probabilidades = clientModelo.connectToServer("libro1",entradasDouble);
        char valorPredicho = prediccion(probabilidades);
        System.out.println("\n------------------ Prediccion ---------------");
        System.out.println(cadena+valorPredicho);
    }
}
