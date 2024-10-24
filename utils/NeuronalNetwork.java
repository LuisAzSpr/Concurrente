package org.example.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NeuronalNetwork implements Serializable {
    private int[] layerSizes; // tamaño de cada capa
    private double[][] neurons; // activaciones de las neuronas
    public double[][][] weights; // pesos de las conexiones
    public double[][] biases; // sesgos (bias) de las neuronas
    public double learningRate = 0.0001; // tasa de aprendizaje
    public double costo = 1e10;

    // Constructor: inicializa la red con los tamaños de las capas
    public NeuronalNetwork(int... layerSizes) {
        this.layerSizes = layerSizes;
        this.neurons = new double[layerSizes.length][];
        this.biases = new double[layerSizes.length][];
        this.weights = new double[layerSizes.length][][];

        // Inicializa las neuronas y los sesgos para cada capa
        for (int i = 0; i < layerSizes.length; i++) {
            this.neurons[i] = new double[layerSizes[i]];

            if (i > 0) { // No hay pesos ni sesgos en la capa de entrada
                this.biases[i] = new double[layerSizes[i]];
                this.weights[i] = new double[layerSizes[i - 1]][layerSizes[i]];

                // Inicializa los pesos y los sesgos aleatoriamente
                for (int j = 0; j < layerSizes[i]; j++) {
                    this.biases[i][j] = Math.random() - 0.5;
                    for (int k = 0; k < layerSizes[i - 1]; k++) {
                        this.weights[i][k][j] = Math.random() - 0.5;
                    }
                }
            }
        }
    }

    // Función de activación (sigmoide)
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    // Derivada de la función sigmoide
    private double sigmoidDerivative(double x) {
        return sigmoid(x) * (1.0 - sigmoid(x));
    }

    // Propagación hacia adelante (feedforward)
    public double[] feedForward(double[] inputs) throws Exception {
        System.arraycopy(inputs, 0, neurons[0], 0, inputs.length); // copiar las entradas
        for (int i = 1; i < layerSizes.length; i++) { // iterar a través de cada capa
            for (int j = 0; j < layerSizes[i]; j++) { // para cada neurona en la capa actual
                double sum = biases[i][j];
                for (int k = 0; k < layerSizes[i - 1]; k++) {
                    sum += neurons[i - 1][k] * weights[i][k][j];
                }
                neurons[i][j] = (i == layerSizes.length - 1) ? sum : sigmoid(sum); // softmax solo en la última capa
            }
        }
        // Aplicar softmax en la capa de salida
        return softmax(neurons[layerSizes.length - 1]);
    }

    // Retropropagación (backpropagation)
    public void backpropagate(double[] target) {
        double[][] errors = new double[layerSizes.length][];

        errors[layerSizes.length-1] = new double[layerSizes[layerSizes.length-1]];
        for (int i = 0; i < target.length; i++) {
            errors[layerSizes.length - 1][i] = target[i] - neurons[layerSizes.length - 1][i];
        }

        // Retropropagación de los errores hacia atrás a través de las capas
        for (int i = layerSizes.length - 2; i > 0; i--) {
            errors[i] = new double[layerSizes[i]];
            for (int j = 0; j < layerSizes[i]; j++) {
                double error = 0;
                for (int k = 0; k < layerSizes[i + 1]; k++) {
                    error += errors[i + 1][k] * weights[i + 1][j][k];
                }
                errors[i][j] = error * sigmoidDerivative(neurons[i][j]);
            }
        }

        // Actualización de pesos y sesgos usando los errores calculados
        for (int i = 1; i < layerSizes.length; i++) {
            for (int j = 0; j < layerSizes[i]; j++) {
                biases[i][j] += errors[i][j] * learningRate;
                for (int k = 0; k < layerSizes[i - 1]; k++) {
                    weights[i][k][j] += neurons[i - 1][k] * errors[i][j] * learningRate;
                }
            }
        }
    }

    public double[] softmax(double[] x) {
        double[] result = new double[x.length];
        double sum = 0.0;

        // Calcular exponencial de cada valor
        for (int i = 0; i < x.length; i++) {
            result[i] = Math.exp(x[i]);
            sum += result[i];
        }

        // Dividir cada valor por la suma total para obtener probabilidades
        for (int i = 0; i < x.length; i++) {
            result[i] /= sum;
        }

        return result;
    }

    public static double categoricalCrossEntropy(double[][] trueLabels, double[][] predictedProbs) {
        int N = trueLabels.length;  // Número de ejemplos
        int C = trueLabels[0].length;  // Número de clases (suponemos que trueLabels y predictedProbs tienen el mismo tamaño)

        double epsilon = 1e-15;  // Para evitar log(0)
        double totalLoss = 0.0;

        // Iterar sobre cada ejemplo
        for (int i = 0; i < N; i++) {
            double loss = 0.0;
            // Iterar sobre cada clase
            for (int j = 0; j < C; j++) {
                // y_ij * log(predicted_ij)
                double trueLabel = trueLabels[i][j];
                double predictedProb = Math.max(predictedProbs[i][j], epsilon);  // Evitar log(0) usando un valor mínimo
                loss += trueLabel * Math.log(predictedProb);
            }
            totalLoss += loss;
        }

        // La entropía cruzada categórica promedio
        return -totalLoss / N;
    }

    // Entrenamiento de la red
    public void train(double[] inputs, double[] targets) throws Exception {
        feedForward(inputs);
        backpropagate(targets);
    }


    public static Map<Character,Integer> mapeo(){
        Map<Character,Integer> valoresCaracteres = new HashMap<>();
        int valor = 0;
        for(char c='a';c<='z';c++){
            valoresCaracteres.put(c,valor++);
        }
        for(char c='0';c<='9';c++){
            valoresCaracteres.put(c,valor++);
        }
        System.out.println("Transformacion -------------------- ");
        for(char c:valoresCaracteres.keySet()){
            System.out.println(c+" -> "+valoresCaracteres.get(c));
        }

        return valoresCaracteres;
    }

    public static String texto(String ruta){
        StringBuilder libro = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                libro.append(linea).append(" "); // Añadir cada línea al StringBuilder
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String libro_contenido = libro.toString();
        String textoSinTildes = libro_contenido
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ","")
                .replaceAll(" ","");

        String libro_limpio = textoSinTildes.replaceAll("[^a-zA-Z0-9\\s]", "");
        return libro_limpio;
    }

    public static ArrayList<Integer> obtenerMapeo(String libro){
        ArrayList<Integer> entrada = new ArrayList<>();
        Map<Character,Integer>valoresCaracteres = mapeo();
        for (int i = 0; i < libro.length(); i++) {
            char c = libro.charAt(i);
            if (valoresCaracteres.containsKey(c)) {
                entrada.add(valoresCaracteres.get(c)); // Agregar el valor numérico seguido de un espacio
            }
        }
        return entrada;
    }


    // Prueba de la red con datos de entrenamiento
    public static void main(String[] args) throws Exception {

        // Limpieza de los datos del libro ----------------------------------
        String libro = texto("../Libros/libro1.txt");
        mapeo();
        ArrayList<Integer> mapeo = obtenerMapeo(libro);

        int n = 1000;
        int m = 6;
        double[][] entradas = new double[n][];
        double[][] salidas = new double[n][36];

        double[][] entradas_train = new double[900][6];
        double[][] entradas_test = new double[100][6];
        double[][] salidas_train = new double[900][36];
        double[][] salidas_test = new double[100][36];

        for(int i=0;i<n;i++){
            if(i<900){
                for(int j=0;j<m;j++){
                    entradas_train[i][j] = mapeo.get(i+j);
                }
                salidas_train[i][mapeo.get(i+m)] = 1;
            }
            else{
                for(int j=0;j<m;j++){
                    entradas_test[i-900][j] = mapeo.get(i+j);
                }
                salidas_test[i-900][mapeo.get(i+m)] = 1;
            }
        }


        int[]layerSizes = new int[]{6,200,50,50,50,36};
        NeuronalNetwork nn = new NeuronalNetwork(6,200,50,50,50,36);
        double coste_min = 1e10;
        double[][][] mejoresPesos = new double[layerSizes.length][][];
        for(int i=1;i<layerSizes.length;i++){
            mejoresPesos[i] = new double[layerSizes[i-1]][layerSizes[i]];
        }
        int contador = 0;
        for (int epoch = 0; epoch < 500; epoch++) {
            for (int i = 0; i < entradas_train.length; i++) {
                nn.train(entradas_train[i], salidas_train[i]);
            }
            double[][] predicciones = new double[100][6];
            for(int j=0;j<entradas_test.length;j++){
                predicciones[j] = nn.feedForward(entradas_test[j]);
            }
            double coste = categoricalCrossEntropy(salidas_test,predicciones);
            if(coste<coste_min){
                contador += 1;
                coste_min = coste;
                mejoresPesos = nn.weights;
                System.out.println("Se actualizaron los pesos por "+contador+" vez , Error = "+coste+ " , epoca = "+epoch);
            }
        }

        double[][]Valores_ = new double[][]{
                {13, 3, 14, 20, 4, 18},
                {0, 13, 19, 4, 18, 3},
                {14, 16, 20, 4, 0, 7},
                {0, 16, 20, 4, 4, 17},
                {0, 0, 13, 19, 4, 18},
                {17, 14, 2, 8, 13, 4},
                {18, 12, 14, 24, 4, 13},
                {19, 14, 3, 20, 17, 14},
                { 17, 14, 13, 14, 2, 0},
                {13, 20, 4, 18, 19, 17}
        };
        for(double[] entradas_prueba : Valores_) {
            double[] valores = nn.feedForward(entradas_prueba);
            int indMax = -1;
            double max = -1e10;
            for (int i = 0; i < valores.length; i++) {
                if (max < valores[i]) {
                    max = valores[i];
                    indMax = i;
                }
            }
            System.out.println(Arrays.toString(valores));
            System.out.println("Valor: " + max + " \nindMax: " + indMax);
        }
    }
}


