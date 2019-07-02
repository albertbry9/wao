import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.nio.file.*;

public class RateDSCorregidos {

    public static  void dataset(String data_path, String delimiter, ArrayList<int[]> data) throws FileNotFoundException {
        FileReader fr = new FileReader(data_path);
        Scanner e = new Scanner(fr);
        e.nextLine();
        while(e.hasNextLine()){
            String[] line = e.nextLine().split(delimiter);
            int[] array = Arrays.stream(line).mapToInt(Integer::parseInt).toArray();
            data.add(array);
        }
    }

    public static HashMap<Integer,Integer>[] Cardinality(ArrayList<int[]> dataset){
        int variable_Number = dataset.get(0).length;

        HashMap<Integer, Integer>[] card = new HashMap[variable_Number];
        for (int i = 0; i< variable_Number; i++) {
            card[i] = new HashMap<>();
        }
        for(int i = 0; i < dataset.size(); i++){
            for (int j = 0; j < dataset.get(i).length; j++) {
                card[j].put(dataset.get(i)[j], card[j].getOrDefault(dataset.get(i)[j],0)+1);
            }
        }
        return card;
    } //DONE

    public static double[] get_cards_dimension(HashMap<Integer, Integer>[] hm){
        double[] cards = new double[hm.length];
        for(int i = 0; i < hm.length; i++)
            cards[i] = (double) hm[i].size();
        return cards;
    } //DONEe

    public static void Rate (ArrayList<int[]> data, ArrayList<ArrayList<int[]>> folds) throws FileNotFoundException{
        Integer variable_Number = data.get(0).length;
        for (int i = 0; i < 10; i++){
            ArrayList<int[]> t = new ArrayList<>();
            folds.add(t);
        }
        int f = 0;
        HashSet<Integer>[] card = new HashSet[variable_Number];
        HashSet<int[]> uniqInstance = new HashSet<>();
        for (int i = 0; i< variable_Number; i++) {
            card[i] = new HashSet<>();
        }
        for(int i = 0; i < data.size(); i++){
            folds.get(f%10).add(data.get(i));
            f++;
            for(int j = 0; j < data.get(i).length; j++){
                card[j].add(data.get(i)[j]);
            }
            uniqInstance.add(data.get(i));
        }
        long aux = 1;

        for (HashSet<Integer> i: card) {
            aux *= i.size();
        }
        System.out.println("Dataset Rate: " + (uniqInstance.size()*1.0/aux*100) + "%");
        System.out.println();
    }

    public static int Count (ArrayList<int[]> dataset, int[] conditions) throws FileNotFoundException{ //DONE
        int cont = 0;
        for(int i = 0; i < dataset.size(); i++){
            boolean f = false;
            for (int j = 0; j < dataset.get(i).length; j++) {
                if(conditions.length > dataset.get(i).length) {
                    break;
                }
                if(j > conditions.length-1) {
                    break;
                }
                if ((dataset.get(i)[j] == conditions[j]) || (conditions[j] == -1)) {
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
        return cont;
    }

    public static double marginal_conjoint_probability(ArrayList<int[]> dataset, int[] conditions, double alpha) throws IOException{
        double number_instances = dataset.size();
        if(number_instances == 0)
            return 0;
        List<Integer> aux = new ArrayList<>();
        for (int i = 0; i < conditions.length;i++) {
            if(conditions[i] == -1)
                continue;
            aux.add(i);
        }
        double multi_cards = 1;
        double count = Count(dataset,conditions);
        HashMap<Integer,Integer>[] cardinality = Cardinality(dataset);
        double[] number_cardinality = get_cards_dimension(cardinality);
        for(int i = 0; i < aux.size();i++){
            multi_cards *= number_cardinality[aux.get(i)];
        }
        return ((count+alpha)/(number_instances+(multi_cards*alpha)));
    }

    public static double conditional_probability(ArrayList<int[]> dataset, int[] condition1,int[] condition2, double alpha)throws IOException {
        double dist1 = marginal_conjoint_probability(dataset,condition1,alpha);
        double dist2 = marginal_conjoint_probability(dataset,condition2,alpha);
        return dist2/dist1;
    }

    public static double aux = 0.0;
    public static ArrayList<Double> aux2 = new ArrayList<>();
    public static ArrayList<int[]> auxSTRING = new ArrayList<>();
    public static ArrayList<Double> graph_distributions(ArrayList<int[]> dataset, int[] cols, double alpha, int idx, ArrayList<Integer>pcc, ArrayList<Double> almacen, HashMap<Integer,Integer>[] cards) throws IOException{
        double[] num_cards = get_cards_dimension(cards);
        if (idx==cols.length){
            int[] arr1 = new int[num_cards.length];
            int[] arr2;
            Arrays.fill(arr1,-1);
            for (int i = 0;i<pcc.size();i++ ){
                arr1[cols[i]] = pcc.get(i);
            }
            arr2 = arr1.clone();
            arr2[cols[cols.length-1]] = -1;
            aux+=conditional_probability(dataset,arr2,arr1,alpha);
            aux2.add(conditional_probability(dataset,arr2,arr1,alpha));
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
            graph_distributions(dataset,cols,alpha,idx+1,pcc,almacen,cards);
            pcc.remove(idx);
        }
        return almacen;
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

    public static ArrayList<ArrayList<Double>> getFactors(ArrayList<int[]> dataset, double alpha,ArrayList<int[]> indices_adyacencia,HashMap<Integer,Integer>[] cards) throws IOException {
        ArrayList<ArrayList<Double>> gran_almacen = new ArrayList<>();
        for(int i = 0; i < indices_adyacencia.size();i++) {
            ArrayList<Integer> pcc = new ArrayList<>();
            ArrayList<Double> almacen = new ArrayList<>();
            gran_almacen.add(graph_distributions(dataset,indices_adyacencia.get(i),alpha,0,pcc,almacen,cards));
        }
        return gran_almacen;
    }

    private final static String FILE_NAME = ".\\src\\dataset.txt"; //Ruta del dataset
    private final static String DELIMITER = "\t" + "";           //Delimitador del dataset
    public static String distConj(ArrayList<Object[]> a, String c, int start,ArrayList<Double> factor,HashMap<Integer,Integer>[] card,ArrayList<ArrayList<Double>> gran_almacen,ArrayList<int[]> x){
        if(start==-1){
            factor.add(getProbGranAlmacen(gran_almacen,c,card,x));
            String auxC = c.replaceAll(".","$0 ");
            System.out.println("P( " + auxC + "): " + factor.get(factor.size()-1));
            return c.substring(1);
        }else{
            for(int j =0; j < a.get(start).length; j++){
                c=a.get(start)[j]+c;
                c=distConj(a,c,start-1,factor,card,gran_almacen,x);
            }
            if(c.length()>1)
                return c.substring(1);
            else
                return "";
        }
    }

    public static Double getProbGranAlmacen(ArrayList<ArrayList<Double>> gran_almacen,String dist,HashMap<Integer,Integer>[] card,ArrayList<int[]> x){
        double prob = 1.0;
        for (int i = 0; i < x.size(); i++){
            int id = getIndex(card,dist,x.get(i));
            prob*=gran_almacen.get(i).get(id);
        }
        return prob;
    }

    public static int getIndex(HashMap<Integer,Integer>[] card,String dist,int[] relac){
        ArrayList<Integer> mult = new ArrayList<>();
        mult.add(1);
        int mult_ant = 1;
        for(int i = 0; i < relac.length; i++){
            mult_ant*=card[relac[i]].size();
            mult.add(mult_ant);
        }
        mult.remove(mult.size()-1);
        int res = 0;
        String[] vals = dist.split("");
        for(int i = 0; i < relac.length; i++){
            int l = Integer.valueOf(vals[relac[i]])*mult.get(i);
            res+=l;
        }
        return res;
    }

    public static ArrayList<int[]> read_matrixNonRepetitive(int[][] matrix){
        ArrayList<int[]> probs = new ArrayList<>();
        for (int i = 0; i < matrix.length;i++){
            ArrayList<Integer> aux = new ArrayList<>();
            aux.add(i);
            for(int j = 0; j < matrix.length; j++) {
                if(matrix[j][i]==1){
                    aux.add(j);
                }
            }
            int[] arr = aux.stream().mapToInt(Integer::intValue).toArray();
            probs.add(arr);
        }
        return probs;
    }
    public static class Inf{
        double prob;
        String clase;
        public Inf(){

        }
        void addProb(double prob){
            this.prob = prob;
        }
        void addClase(String clase){
            this.clase = clase;
        }
        public double getProb(){
            return this.prob;
        }
        public String getClase(){
            return this.clase;
        }
    }
    public static Inf inferencia2(ArrayList<Double> factor,int[] var, int[] val, ArrayList<Object[]> a, HashMap<Integer,Integer>[] card,int[] relac){
        String consulta = "";
        int find = 0;
        for(int i = 0; i < var.length; i++){
            for(int j = 0; j < var.length; j++){
                if(var[j] == i){
                    if(val[j]==-1){
                        consulta+="?";
                        find = var[j];
                    }else{
                        consulta+=val[j];
                    }
                }
            }
        }
        double mayor = -1.0;
        String clase = "";
        for(int i = 0; i < a.get(find).length; i++){
            if(factor.get(getIndex(card,consulta.replace("?",a.get(find)[i].toString()),relac)) > mayor){
                mayor = factor.get(getIndex(card,consulta.replace("?",a.get(find)[i].toString()),relac));
                clase = a.get(find)[i].toString();
            }
        }
        Inf res = new Inf();
        res.addProb(mayor);
        res.addClase(clase);
        return res;
    }
    public static void matriz_confusion(ArrayList<int[]> data_test, int pred, int[] relac, ArrayList<Object[]> a,int[][] conf,HashMap<Integer,Integer>[] card,ArrayList<Double> factor, int[][] m_final){
        for(int i = 0; i < data_test.size(); i++){
            int[] tmp = data_test.get(i).clone();
            tmp[pred] = -1;
            Inf res = inferencia2(factor,relac,tmp,a,card,relac);
            conf[Integer.valueOf(res.getClase())][Integer.valueOf(data_test.get(i)[pred])]++;
            m_final[Integer.valueOf(res.getClase())][Integer.valueOf(data_test.get(i)[pred])]++;
        }
    }
    public static ArrayList<ArrayList<int[]>> getFolds(ArrayList<int[]> data){
        ArrayList<ArrayList<int[]>> folds = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            ArrayList<int[]> t = new ArrayList<>();
            folds.add(t);
        }
        int f = 0;
        for(int i = 0; i < data.size(); i++){
            folds.get(f%10).add(data.get(i));
            f++;
        }
        return folds;
    }
    public static void prettyPrintMatriz(int[][] matriz){
        System.out.print("    ");
        for(int i = 0; i < matriz.length; i++){
            System.out.print(i+"  ");
        }
        System.out.print("  <---- Clases reales");
        System.out.println();
        System.out.println();
        for(int i = 0; i < matriz.length; i++){
            System.out.print(i+"   ");
            for(int j = 0; j < matriz.length; j++){
                System.out.print(matriz[i][j]);
                System.out.print("  ");
            }
            System.out.println();
        }
    }
    public static int[][] medidas(int[][] m){
        int[][] m_tmp = new int[m.length][3];
        for(int i = 0; i < m.length; i++){
            for(int j = 0; j < m.length; j++){
                m_tmp[i][j] = 0;
            }
        }
        for(int i = 0; i < m.length; i++){
            for(int j = 0; j < m.length; j++){
                if(i==j){
                    m_tmp[i][0] += m[i][j];
                }else{
                    m_tmp[i][1] += m[i][j];
                    m_tmp[j][2] += m[i][j];
                }
            }
        }
        return m_tmp;
    }
    public static void main(String[] args) throws IOException {
        long start_time = System.nanoTime();
        double alpha = 1.0; //Alfa para Dirichlet
        int[][] adj = {
                {0, 0, 1, 1, 1},
                {0, 0, 1, 1, 0},
                {0, 0, 0, 1, 1},
                {0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0}
        };
        ArrayList<int[]> indices_adyacencia = read_matrix(adj);
        ArrayList<int[]> dataset_original = new ArrayList<>();
        dataset(FILE_NAME,DELIMITER,dataset_original);
        ArrayList<ArrayList<int[]>> folds = new ArrayList<>();
        HashMap<Integer,Integer>[] card_original = Cardinality(dataset_original);
        int pred = 4;
        int[][] matriz_final = new int[card_original[pred].size()][card_original[pred].size()];
        boolean isRandom = false;
        if(isRandom){
            ArrayList<int[]> shuffle = new ArrayList<>();
            for (int[] t:dataset_original) {
                shuffle.add((int[])t.clone());
            }
            Collections.shuffle(shuffle);
            Rate(shuffle,folds);
        }else{
            Rate(dataset_original,folds); //Rate y folds del dataset
        }
        for(int i = 0; i < 10; i++){
            System.out.println("ITERACION: "+i);
            ArrayList<int[]> test = new ArrayList<>();
            ArrayList<int[]> training = new ArrayList<>();
            for(int j = 0; j < 10; j++){
                if(i == j){
                    test = folds.get(i);
                }else{
                    for(int k = 0; k < folds.get(j).size(); k++){
                        training.add(folds.get(j).get(k));
                    }
                }
            }
            HashMap<Integer,Integer>[] card = Cardinality(training);
            ArrayList<ArrayList<Double>> gran_almacen = getFactors(training,alpha,indices_adyacencia,card);
            ArrayList<Object[]> a = new ArrayList<>();
            for (int ii = 0; ii < card.length; ii++){
                a.add(card[ii].keySet().toArray());
            }
            ArrayList<Double> factor = new ArrayList<>();
            ArrayList<int[]> nonRep = read_matrixNonRepetitive(adj);
            System.out.println("Distribucion conjunta del grafo: ");
            distConj(a,"",card.length-1,factor,card,gran_almacen,nonRep);
            int[] newRelac = new int[a.size()];
            for(int ii = 0; ii < newRelac.length; ii++){
                newRelac[ii] = ii;
            }
            int[][] matriz = new int[a.get(pred).length][a.get(pred).length];
            for(int ii = 0; ii < matriz.length; ii++){
                for(int jj = 0; jj < matriz.length; jj++){
                    matriz[ii][jj] = 0;
                }
            }
            System.out.println();
            System.out.println("Matriz de confusion de la iteracion: " + i);
            System.out.println();
            matriz_confusion(test,pred,newRelac,a,matriz,card,factor,matriz_final);
            prettyPrintMatriz(matriz);
            if(i != 9){
                System.out.println("---------------------------------------------------------------------");
            }
        }
        System.out.println();
        System.out.println("===========================================");
        int[][] new_medidas = medidas(matriz_final);
        System.out.println("Matriz de confusion final");
        System.out.println();
        prettyPrintMatriz(matriz_final);
        System.out.println();
        double acc = 0.0;
        for(int i = 0; i < matriz_final.length; i++){
            acc += matriz_final[i][i];
        }
        for(int i = 0; i < new_medidas.length; i++){
            System.out.println("Medidas para la clase: "+i);
            System.out.println();
            double precision = new_medidas[i][0]*1.0/(new_medidas[i][0]+new_medidas[i][1])*1.0;
            double recall = new_medidas[i][0]*1.0/(new_medidas[i][0]+new_medidas[i][2])*1.0;
            double f1 = (2*precision*recall)/(precision+recall);
            System.out.println("precision = " + precision);
            System.out.println("recall = " + recall);
            System.out.println("f1 = " + f1);
            System.out.println("-------------------------------");
        }
        acc = acc/dataset_original.size();
        System.out.println("accuracy = " + acc);
        System.out.println("---------------------------------------");
        long finish_time = System.nanoTime() - start_time;
        System.out.println("Tiempo transcurrido: " + ((double) finish_time / 1_000_000_000.0) + "s");
    }
}