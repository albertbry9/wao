import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.*;

public class RateDSCorregidos {
    public static HashMap<String,Integer>[] Cardinality(String data_path, String delimiter)throws FileNotFoundException{
        FileReader fr = new FileReader(data_path);
        Scanner e = new Scanner(fr);
        int variable_Number = e.nextLine().split(delimiter).length;

        HashMap<String, Integer>[] card = new HashMap[variable_Number];
        for (int i = 0; i< variable_Number; i++) {
            card[i] = new HashMap<>();
        }
        while(e.hasNextLine()) {
            String[] line = e.nextLine().split(delimiter);
            for (int i = 0; i < line.length; i++) {
                card[i].put(line[i], card[i].getOrDefault(line[i],0)+1);
            }
        }
        return card;
    } //DONE
    public static double[] get_cards_dimension(HashMap<String, Integer>[] hm){
        double[] cards = new double[hm.length];
        for(int i = 0; i < hm.length; i++)
            cards[i] = (double) hm[i].size();
        return cards;
    } //DONEe
    public static void Rate (String data_path, String delimiter) throws FileNotFoundException{
        FileReader fr = new FileReader(data_path);
        Scanner e = new Scanner(fr);

        Integer variable_Number = e.nextLine().split(delimiter).length;

        HashSet<String>[] card = new HashSet[variable_Number];
        HashSet<String> uniqInstance = new HashSet<>();

        for (int i = 0; i< variable_Number; i++) {
            card[i] = new HashSet<>();
        }
        while(e.hasNextLine()) {
            String[] line = e.nextLine().split(delimiter);
            for (int i = 0; i < line.length; i++) {
                card[i].add((line[i]));
            }
            uniqInstance.add(String.join("",line));
        }
        e.close();
        long aux = 1;

        for (HashSet<String> i: card) {
            aux *= i.size();
        }
        System.out.println("Dataset Rate: " + (uniqInstance.size()*1.0/aux*100) + "%");
        System.out.println("");
    }
    public static int Count (String data_path, String delimiter, String[] conditions) throws FileNotFoundException{ //DONE
        FileReader fr = new FileReader(data_path);
        Scanner e = new Scanner(fr);
        e.nextLine();
        int cont = 0;
        while (e.hasNextLine()) {
            boolean f = false;
            String[] line = e.nextLine().split(delimiter);
            for (int i = 0; i < line.length; i++) {
                if(conditions.length > line.length) {
                    break;
                }
                if(i > conditions.length-1) {
                    break;
                }
                if (line[i].equals(conditions[i]) || conditions[i].equals("-1")) {
                    f = true;
                }
                else {
                    f = false;
                    break;
                }
            }
            if (f)
                cont++;
        }
        e.close();
        return cont;
    }
    public static double marginal_conjoint_probability(String data_path, String delimiter, String[] conditions, int alpha) throws IOException{
        Path path = Paths.get(data_path);
        double number_instances = Files.lines(path).count()-1;
        if(number_instances == 0)
            return 0;
        List<Integer> aux = new ArrayList<>();
        for (int i = 0; i < conditions.length;i++) {
            if(conditions[i] == "-1")
                continue;
            aux.add(i);
        }
        double multi_cards = 1;
        double count = Count(data_path,delimiter,conditions);
        HashMap<String,Integer>[] cardinality = Cardinality(data_path,delimiter);
        double[] number_cardinality = get_cards_dimension(cardinality);
        for(int i = 0; i < aux.size();i++){
            multi_cards *= number_cardinality[aux.get(i)];
        }
        return ((count+alpha)/(number_instances+(multi_cards*alpha)));
    }
    public static double conditional_probability(String data_path, String delimiter, String[] condition1,String[] condition2, int alpha)throws IOException {
        double dist1 = marginal_conjoint_probability(data_path, delimiter,condition1,alpha);
        double dist2 = marginal_conjoint_probability(data_path,delimiter,condition2,alpha);
        return dist2/dist1;
    }
    public static double aux = 0.0;
    public static ArrayList<Double> aux2 = new ArrayList<>();
    public static ArrayList<String[]> auxSTRING = new ArrayList<>();
    public static ArrayList<Double> graph_distributions(String data_path, String delimiter, int[] cols, int alpha, int idx, ArrayList<Integer>pcc, ArrayList<Double> almacen) throws IOException{
        HashMap<String,Integer>[] cards = Cardinality(data_path,delimiter);
        double[] num_cards = get_cards_dimension(cards);
        if (idx==cols.length){
            String[] arr1 = new String[num_cards.length];
            String[] arr2;
            Arrays.fill(arr1,"-1");
            for (int i = 0;i<pcc.size();i++ ){
                arr1[cols[i]] = pcc.get(i).toString();
            }
            arr2 = arr1.clone();
            arr2[cols[cols.length-1]] = "-1";
            aux+=conditional_probability(data_path,delimiter,arr2,arr1,alpha);
            aux2.add(conditional_probability(data_path,delimiter,arr2,arr1,alpha));
            auxSTRING.add(arr1);
            if(aux2.size()==num_cards[cols[cols.length-1]]){
                double aux3 = 0;
                for(int k = 0; k<aux2.size();k++){
                    aux3 += aux2.get(k)/aux;
                    almacen.add(aux2.get(k)/aux);
                    System.out.println("P( " + Arrays.toString(auxSTRING.get(k))+ " )= " +aux2.get(k)/aux+"   "+((char)(65+cols[cols.length-1]))+ " es hijo.");
                }
                System.out.println("Suma de probabilidades: "+aux3);
                System.out.println("");
                aux2 = new ArrayList<>();
                auxSTRING = new ArrayList<>();
                aux = 0.0;
            }
            return almacen;
        }
        double c = num_cards[cols[idx]];
        for(int i = 0; i< c;i++){
            pcc.add(i);
            graph_distributions(data_path,delimiter,cols,alpha,idx+1,pcc,almacen);
            pcc.remove(idx);
        }
        return almacen;
    }
    public static double aux_conjunta = 0;
    public static void conjoint_distribution(String data_path, String delimiter, int alpha, int idx, ArrayList<Integer>pcc)throws IOException{
        HashMap<String,Integer>[] cards = Cardinality(data_path,delimiter);
        double[] num_cards = get_cards_dimension(cards);
        int[] cols = IntStream.rangeClosed(0,num_cards.length-1).toArray();
        if(idx == cols.length){
            String[] arr1 = new String[num_cards.length];
            Arrays.fill(arr1,"-1");
            for (int i = 0;i<pcc.size();i++ ){
                arr1[cols[i]] = pcc.get(i).toString();
            }
            aux_conjunta+=marginal_conjoint_probability(data_path,delimiter,arr1,alpha);
            System.out.println("P( " + Arrays.toString(arr1) +" ): " + marginal_conjoint_probability(data_path,delimiter,arr1,alpha));
            return;
        }
        double c = num_cards[cols[idx]];
        for(int i = 0; i< c;i++){
            pcc.add(i);
            conjoint_distribution(data_path,delimiter,alpha,idx+1,pcc);
            pcc.remove(idx);
        }
    }
    public static ArrayList<int[]> read_matrix(int[][] matrix){
        ArrayList<int[]> probs = new ArrayList<>();
        ArrayList<Integer> aux = new ArrayList<>();
        for (int i = 0; i < matrix.length;i++){
            aux.add(i);
            for(int j = 0; j < matrix.length; j++) {
                if(matrix[j][i]==1){
                    aux.add(0,j);
                }
            }
            int[] arr = aux.stream().mapToInt(Integer::intValue).toArray();
            probs.add(arr);
            aux = new ArrayList<>();
        }
        return probs;
    }
    private final static String FILE_NAME = ".\\src\\help.txt"; //Ruta del dataset
    private final static String DELIMITER = "\t" + "";           //Delimitador del dataset
    public static void main(String[] args) throws IOException {
        Rate(FILE_NAME,DELIMITER); //Rate del dataset
        int alpha = 1; //Alfa para Dirichlet
        /*int[][] adj = {{0,0,0,0}, //A   //Matriz de adyacencia
                       {1,0,0,0}, //B
                       {0,0,0,1}, //C
                       {0,0,0,0}};//D*/
                     // A,B,C,D,E,F,G
        int[][] adj = {{0,1,1,0,0,0,0},
                       {0,0,1,1,0,0,0},
                       {0,0,0,0,0,0,0},
                       {0,0,0,0,1,0,1},
                       {0,0,0,0,0,1,0},
                       {0,0,1,0,0,0,0},
                       {0,0,1,0,0,0,0}};
        ArrayList<int[]> x = read_matrix(adj);
        System.out.println("Probabilidades segun grafo: ");
        System.out.println();
        ArrayList<ArrayList<Double>> gran_almacen = new ArrayList<>();
        for(int i = 0; i < x.size();i++) {
            ArrayList<Integer> pcc = new ArrayList<>();
            ArrayList<Double> almacen = new ArrayList<>();
            gran_almacen.add(graph_distributions(FILE_NAME,DELIMITER,x.get(i),alpha,0,pcc,almacen));
        }
        System.out.println("Fin de las probabilidades condicionales/marginales para cada elemento del grafo");
        System.out.println();
        System.out.println("Probabilidades conjuntas: ");
        System.out.println();
        ArrayList<Integer> pcc = new ArrayList<>();
        //conjoint_distribution(FILE_NAME,DELIMITER,alpha,0,pcc);
        System.out.println("Suma de probabilidades conjuntas: "+aux_conjunta);
    }
}
