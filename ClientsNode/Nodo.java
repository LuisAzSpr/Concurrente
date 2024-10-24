package org.example.ClientsNode;


import org.example.utils.Data;
import org.example.utils.NeuronalNetwork;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

import static org.example.utils.NeuronalNetwork.categoricalCrossEntropy;

public class Nodo {
    private static Map<Character,Integer> valoresCaracteres = new HashMap<>();
    private int [] layerSizes = new int[]{6,50,50,50,50,50,50,36};

    public ObjectOutputStream oos;
    public ObjectInputStream ois;

    public Nodo() throws IOException {
        mapeo();
        Socket socket = new Socket("127.0.1.1", 30100);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
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

    private void connectToServer(){
        try{
            while(true) {
                Data data = (Data) ois.readObject(); // leemos los datos que han sido enviados
                NeuronalNetwork bestModel = train(data.train, data.test); // entrenamos el modelo con los datos y tomamos al mejor
                oos.writeObject(bestModel); // mandamos el mejor modelo
                oos.flush();  // esperamos a que se complete
                System.out.println("Objeto serializado con éxito.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NeuronalNetwork train(String dataTrain,String dataTest) throws Exception {
        double coste_min = 1e10;
        int contador = 0;
        NeuronalNetwork nn = new NeuronalNetwork(layerSizes);
        double[][][] mejoresPesos = new double[layerSizes.length][][];
        double[][] mejoresBias = new double[layerSizes.length][];

        String dataTrainLimpia = limpiarLibro(dataTrain);
        String dataTestLimpia = limpiarLibro(dataTest);

        int xSize = 6;
        int trainSize = dataTrainLimpia.length() - 2*xSize - 1;
        int testSize = dataTestLimpia.length() - 2*xSize - 1;

        double[][] entradas_train = obtenerEntradas(dataTrainLimpia,6,trainSize);
        double[][] salidas_train = obtenerSalidas(dataTrainLimpia,6,trainSize);
        double[][] entradas_test = obtenerEntradas(dataTestLimpia,6,testSize);
        double[][] salidas_test = obtenerSalidas(dataTestLimpia,6,testSize);

        for (int epoch = 0; epoch < 20; epoch++) {
            for (int i = 0; i < entradas_train.length; i++) {
                nn.train(entradas_train[i], salidas_train[i]);
            }
            double[][] predicciones = new double[testSize][36];
            for(int j=0;j<entradas_test.length;j++){
                predicciones[j] = nn.feedForward(entradas_test[j]);
            }
            double coste = categoricalCrossEntropy(salidas_test,predicciones);
            System.out.println("Epoca = "+epoch);
            if(coste<coste_min){
                contador += 1;
                coste_min = coste;
                mejoresPesos = nn.weights;
                mejoresBias = nn.biases;
                System.out.println("Se actualizaron los pesos por "+contador+" vez , Error = "+coste);
            }
        }
        NeuronalNetwork bestModel = new NeuronalNetwork(layerSizes);
        bestModel.weights = mejoresPesos;
        bestModel.biases = mejoresBias;
        bestModel.costo = coste_min;
        return bestModel;
    }


    public static double[][] obtenerEntradas(String texto,int tamanioEntrada,int instancias){
        double[][] entradas = new double[instancias][tamanioEntrada];
        for(int i=0;i<instancias;i++){
            for(int j=0;j<tamanioEntrada;j++){
                entradas[i][j] = valoresCaracteres.get(texto.charAt(i+j));
            }
        }
        return entradas;
    }

    public static double[][] obtenerSalidas(String texto,int tamanioEntrada,int instancias) {
        double[][] salidas = new double[instancias][valoresCaracteres.size()];
        for (int i = 0; i < instancias; i++) {
            int valor = valoresCaracteres.get(texto.charAt(i + tamanioEntrada));
            salidas[i][valor] = 1;
        }
        return salidas;
    }

    public static String limpiarLibro(String libro){
        String libroContenido = libro.toString().toLowerCase();
        String libroSinTildes = libroContenido
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ","")
                .replaceAll(" ","");
        String libroLimpio = libroSinTildes.replaceAll("[^a-zA-Z0-9\\s]", "");
        return libroLimpio;
    }
    public static void main(String[] args) throws IOException {
        Nodo nodo = new Nodo();
        nodo.connectToServer();
    }
}


