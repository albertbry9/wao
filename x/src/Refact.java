import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import  weka.filters.unsupervised.attribute.Reorder;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Refact {
	public static generalClass sameFunctions = new generalClass();

	public static class res_marg_conj{
		private double res;
		private double count;
		public res_marg_conj(double res, double count){
			this.res = res;
			this.count = count;
		}
		public double getRes(){
			return this.res;
		}
		public double getCount(){
			return this.count;
		}
	}

	public static class res_graph_dist{
		private ArrayList<Double> almacen;
		private ArrayList<Double> almacenCount;
		public res_graph_dist(ArrayList<Double> almacen, ArrayList<Double> almacenCount){
			this.almacen = almacen;
			this.almacenCount = almacenCount;
		}
		public ArrayList<Double> getAlmacen(){
			return this.almacen;
		}
		public ArrayList<Double> getAlmacenCount(){
			return this.almacenCount;
		}
	}

	public static class res_factor{
		private ArrayList<ArrayList<Double>> gran_almacen = new ArrayList<>();
		private ArrayList<ArrayList<Double>> gran_count = new ArrayList<>();
		public res_factor(ArrayList<ArrayList<Double>> gran_almacen, ArrayList<ArrayList<Double>> gran_count){
			this.gran_almacen = gran_almacen;
			this.gran_count = gran_count;
		}
		public ArrayList<ArrayList<Double>> getGran_almacen(){
			return this.gran_almacen;
		}
		public ArrayList<ArrayList<Double>> getGran_count(){
			return this.gran_count;
		}
	}

	public static IniStats readArff(String ARFF_NAME) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(ARFF_NAME));
		ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
		Instances data = arff.getData();
		ArrayList<int[]> dataIni = sameFunctions.InstancesToArray(data);
		ArrayList<String> permNames= new ArrayList<>();
		String str = "";
		for(int i = 0; i < data.numAttributes(); i++){
			str += (i+1);
		}
		sameFunctions.printPermutn(str,"",data,permNames);
		ArrayList<ArrayList<int[]>> folds = new ArrayList<>();
		HashMap<Integer,Integer>[] card = new HashMap[data.numAttributes()];
		int f = 0;
		for(int i = 0; i < 10; i++){
			ArrayList<int[]> t = new ArrayList<>();
			folds.add(t);
		}
		for(int i = 0; i < card.length; i++){
			card[i] = new HashMap<>();
		}
		for(int i = 0; i < card.length; i++){
			int[] val = data.attributeStats(i).nominalCounts;
			for(int j = 0; j < val.length; j++){
				card[i].put(j,val[j]);
			}
		}
		for(int i = 0; i < data.size(); i++){
			double[] tmpd = data.get(i).toDoubleArray();
			int[] tmp = new int[tmpd.length];
			for(int j = 0; j < tmp.length; j++){
				tmp[j] = (int) tmpd[j];
			}
			folds.get(f%10).add(tmp);
			f++;
		}
		IniStats ret = new IniStats(data.numAttributes(),data.size(),folds,card,dataIni);
		ret.addPermNames(permNames);
		ret.addData(data);
		return ret;
	}

	public static Double getProbGranAlmacen(ArrayList<ArrayList<Double>> gran_almacen,String dist,HashMap<Integer,Integer>[] card,ArrayList<int[]> x){
		double prob = 1.0;
		for (int i = 0; i < x.size(); i++){
			int id = getIndex(card,dist,x.get(i));
			prob*=gran_almacen.get(i).get(id);
		}
		return prob;
	}

	public static String distConj(ArrayList<Object[]> a, String c, int start,ArrayList<Double> factor,HashMap<Integer,Integer>[] card,ArrayList<ArrayList<Double>> gran_almacen,ArrayList<int[]> x){
		if(start==-1){
			factor.add(getProbGranAlmacen(gran_almacen,c,card,x));
			String auxC = c.replaceAll(".","$0 ");
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

	public static void generalFunction(int[][] adj, int pred, IniStats ini, double alpha, ArrayList<int[]> indices_adyacencia) throws IOException {
		ArrayList<ArrayList<int[]>> folds = ini.getFolds();
		int[][] matriz_final = new int[ini.getCard()[pred].size()][ini.getCard()[pred].size()];
		for(int i = 0; i < 10; i++){
			ArrayList<int[]> test = folds.get(i);
			ArrayList<int[]> training = new ArrayList<>();
			for(int j = 0; j < 10; j++){
				if(j != i){
					training.addAll(folds.get(j));
				}
			}
			HashMap<Integer,Integer>[] card = sameFunctions.Cardinality(training);
			res_factor resFactor = sameFunctions.getFactors(ini.getDataset(),alpha,indices_adyacencia,card,new HashMap[0],false);
			ArrayList<ArrayList<Double>> gran_almacen = resFactor.getGran_almacen();
			ArrayList<Object[]> a = new ArrayList<>();
			for (int ii = 0; ii < card.length; ii++){
				a.add(card[ii].keySet().toArray());
			}
			ArrayList<Double> factor = new ArrayList<>();
			distConj(a,"",card.length-1,factor,card,gran_almacen,indices_adyacencia);
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
			matriz_confusion(test,pred,newRelac,a,matriz,card,factor,matriz_final);
		}
		System.out.println("Modelo: ");
		for(int i = 0; i < adj.length; i++){
			for(int j = 0; j < adj.length; j++){
				System.out.print(adj[i][j]);
			}
			System.out.println();
		}
		System.out.println("-----------------------------------");
		int[][] new_medidas = medidas(matriz_final);
		prettyPrintMatriz(matriz_final);
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
		acc = acc/ini.getDataset().size();
		System.out.println("accuracy = " + acc);
		System.out.println("---------------------------------------");
	}

	public static class res_comparison{
		private double H;
		private double AIC;
		private double MDL;
		private int[][] adj;
		private ArrayList<int[]> indices_adyacencia;
		public res_comparison(double H, double AIC, double MDL, int[][] adj, ArrayList<int[]> indices_adyacencia){
			this.H = H;
			this.AIC = AIC;
			this.MDL = MDL;
			this.adj = adj;
			this.indices_adyacencia = indices_adyacencia;
		}
		public double getH(){
			return this.H;
		}
		public double getAIC(){
			return this.AIC;
		}
		public double getMDL(){
			return this.MDL;
		}
		public int[][] getAdj(){
			return this.adj;
		}
		public ArrayList<int[]> getIndices_adyacencia(){
			return this.indices_adyacencia;
		}
	}

	public static  boolean isCyclic(int i, boolean[] visit, boolean[] stack, int[] matrix, int v){
		if(stack[i]){
			return true;
		}
		if(visit[i]){
			return false;
		}
		visit[i] = true;
		stack[i] = true;
		ArrayList<Integer> child = new ArrayList<>();
		for(int j = 0; j < v; j++){
			if(j != i && matrix[(i*v)+j] == 1){
				child.add(j);
			}
		}
		for (Integer c:child) {
			if(isCyclic(c,visit,stack,matrix,v)){
				return true;
			}
		}
		stack[i] = false;
		return false;
	}
	public static boolean isCycGraph(int[] matrix, int v){
		boolean[] visit = new boolean[v];
		boolean[] stack = new boolean[v];
		for(int i = 0; i < v; i++){
			visit[i] = false;
		}
		for(int i = 0; i < v; i++){
			if(isCyclic(i,visit,stack,matrix,v)){
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> writeFiles(int num_threads, int n, long a) throws IOException {
		ArrayList<String> names = new ArrayList<>();
		ArrayList<File> dags_files = new ArrayList<>();
		ArrayList<FileOutputStream> fos_list = new ArrayList<>();
		ArrayList<BufferedWriter> bw_list = new ArrayList<>();
		for(int i = 0; i < num_threads; i++){
			String name = "dags" + i + ".txt";
			names.add(name);
			File file = new File(name);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			if(file.exists()){
				PrintWriter writer = new PrintWriter(file);
				writer.print("");
				writer.close();
			}
			dags_files.add(file);
			fos_list.add(fos);
			bw_list.add(bw);
		}
		Permutacion pm = new Permutacion();
		for(int i = 0; i < a; i++){
			if(i%(n+1) == 0){
				int[] nv = {0};
				pm.add_att(nv);
			}else{
				int[] nv = {0,1};
				pm.add_att(nv);
			}
		}
		boolean tmp = true;
		int cont_files = 0;
		while(tmp){
			int[] res = pm.permutar();
			if(res == null){
				tmp = false;
			}else{
				if(!isCycGraph(res,n)){
					String matrix = "";
					for(int i = 0; i < res.length; i++){
						matrix += res[i];
					}
					bw_list.get(cont_files).write(matrix);
					bw_list.get(cont_files).newLine();
					cont_files++;
					cont_files = cont_files % num_threads;
				}
			}
		}
		for(int i = 0; i < bw_list.size(); i++){
			bw_list.get(i).close();
		}
		return names;
	}

	public static int[][] arrToMatrix(int[] arr, int n){
		int[][] ret = new int[n][n];
		for(int i = 0; i < arr.length; i++){
			ret[i/n][i%n] = arr[i];
		}
		return ret;
	}

	public static class forK2Perm{
		private res_comparison mayH;
		private res_comparison mayAIC;
		private res_comparison mayMDL;
		public forK2Perm(){

		}
		public void addMayH(res_comparison mayH){
			this.mayH = mayH;
		}
		public void addMayAIC(res_comparison mayAIC){
			this.mayAIC = mayAIC;
		}
		public void addMayMDL(res_comparison mayMDL){
			this.mayMDL = mayMDL;
		}
		public res_comparison getMayH(){
			return this.mayH;
		}
		public res_comparison getMayAIC(){
			return this.mayAIC;
		}
		public res_comparison getMayMDL(){
			return this.mayMDL;
		}
	}

	public static forK2Perm k2NotPermutation(int n, IniStats ini, double alpha, int pred, boolean showResults, ArrayList<int[]> new_dataset) throws IOException {
		Permutacion pm = new Permutacion();
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				if(j <= i){
					int[] t = {0};
					pm.add_att(t);
				}else{
					int[] t = {0,1};
					pm.add_att(t);
				}
			}
		}
		boolean tmp = true;
		int cont = 0;
		double valH = -9999;
		double valAIC = -9999;
		double valMDL = -9999;
		res_comparison mayH = null;
		res_comparison mayAIC = null;
		res_comparison mayMDL = null;
		ArrayList<int[]> dataParam = null;
		if(showResults){
			dataParam = ini.getDataset();
		}else{
			dataParam = new_dataset;
		}
		while(tmp){
			int[] arrMat = pm.permutar();
			if(arrMat == null){
				tmp = false;
			}else{
				int[][] adj = arrToMatrix(arrMat,n);
				ArrayList<int[]> indices_adyacencia = sameFunctions.read_matrix(adj);
				res_comparison comparison = sameFunctions.forComparison(adj,indices_adyacencia,dataParam,alpha,ini.getCard());
				if(cont == 0){
					valH = comparison.getH();
					mayH = comparison;
				}else{
					if(valH > comparison.getH()){
						break;
					}else{
						valH = comparison.getH();
						mayH = comparison;
					}
				}
				cont++;
			}
		}
		pm.endPermute();
		tmp = true;
		cont = 0;
		while(tmp){
			int[] arrMat = pm.permutar();
			if(arrMat == null){
				tmp = false;
			}else{
				int[][] adj = arrToMatrix(arrMat,n);
				ArrayList<int[]> indices_adyacencia = sameFunctions.read_matrix(adj);
				res_comparison comparison = sameFunctions.forComparison(adj,indices_adyacencia,dataParam,alpha,ini.getCard());
				if(cont == 0){
					valAIC = comparison.getAIC();
					mayAIC = comparison;
				}else{
					if(valAIC > comparison.getAIC()){
						break;
					}else{
						valAIC = comparison.getAIC();
						mayAIC = comparison;
					}
				}
				cont++;
			}
		}
		pm.endPermute();
		tmp = true;
		cont = 0;
		while(tmp){
			int[] arrMat = pm.permutar();
			if(arrMat == null){
				tmp = false;
			}else{
				int[][] adj = arrToMatrix(arrMat,n);
				ArrayList<int[]> indices_adyacencia = sameFunctions.read_matrix(adj);
				res_comparison comparison = sameFunctions.forComparison(adj,indices_adyacencia,dataParam,alpha,ini.getCard());
				if(cont == 0){
					valMDL = comparison.getMDL();
					mayMDL = comparison;
				}else{
					if(valMDL > comparison.getMDL()){
						break;
					}else{
						valMDL = comparison.getMDL();
						mayMDL = comparison;
					}
				}
				cont++;
			}
		}
		if(showResults){
			System.out.println("=====================================================");
			System.out.println("Resultados para k2");
			System.out.println("-----------------------------------------------------");
			System.out.println("Mejor modelo segun Entropia");
			System.out.println("H: " + mayH.getH());
			System.out.println("---------------------------------------------------");
			generalFunction(mayH.getAdj(),pred,ini,alpha,mayH.getIndices_adyacencia());
			System.out.println("---------------------------------------------------");
			System.out.println("Mejor modelo segun Akaike");
			System.out.println("Akaike: " + mayAIC.getAIC());
			System.out.println("---------------------------------------------------");
			generalFunction(mayAIC.getAdj(),pred,ini,alpha,mayAIC.getIndices_adyacencia());
			System.out.println("---------------------------------------------------");
			System.out.println("Mejor modelo segun MDL");
			System.out.println("MDL: " + mayMDL.getMDL());
			System.out.println("---------------------------------------------------");
			generalFunction(mayMDL.getAdj(),pred,ini,alpha,mayMDL.getIndices_adyacencia());
		}
		forK2Perm res_k2 = new forK2Perm();
		res_k2.addMayH(mayH);
		res_k2.addMayAIC(mayAIC);
		res_k2.addMayMDL(mayMDL);
		return res_k2;
	}
	public static void k2Permutation(int n, IniStats ini, double alpha, int pred,ArrayList<String> permNames) throws Exception {
		System.out.println("==========================================================");
		System.out.println();
		System.out.println("Aqui empieza k2 con diferente orden");
		System.out.println();
		System.out.println("==========================================================");
		double valH = -9999;
		double valAIC = -9999;
		double valMDL = -9999;
		res_comparison mayH = null;
		res_comparison mayAIC = null;
		res_comparison mayMDL = null;
		String orden_H = "";
		String orden_AIC = "";
		String orden_MDL = "";
		int cont =0;
		for(int i = 0; i < permNames.size(); i++){
			Reorder reorder = new Reorder();
			reorder.setAttributeIndices(permNames.get(i));
			reorder.setInputFormat(ini.getData());
			Instances new_data = Filter.useFilter(ini.getData(),reorder);
			ArrayList<int[]> new_data_comp = sameFunctions.InstancesToArray(new_data);
			forK2Perm compK2 = k2NotPermutation(n,ini,alpha,pred,false,new_data_comp);
			cont++;
			if(compK2.getMayH().getH() > valH){
				valH = compK2.getMayH().getH();
				mayH = compK2.getMayH();
				orden_H = permNames.get(i);

			}
			if(compK2.getMayAIC().getAIC() > valAIC){
				valAIC = compK2.getMayAIC().getAIC();
				mayAIC = compK2.getMayAIC();
				orden_AIC = permNames.get(i);
			}
			if(compK2.getMayMDL().getMDL() > valMDL){
				valMDL = compK2.getMayMDL().getMDL();
				mayMDL = compK2.getMayMDL();
				orden_MDL = permNames.get(i);
			}
		}
		System.out.println("Mejor modelo segun Entropia");
		System.out.println("Orden de las variables: " + orden_H);
		System.out.println("H: " + mayH.getH());
		System.out.println("---------------------------------------------------");
		generalFunction(mayH.getAdj(),pred,ini,alpha,mayH.getIndices_adyacencia());
		System.out.println("---------------------------------------------------");
		System.out.println("Mejor modelo segun Akaike");
		System.out.println("Orden de las variables: " + orden_AIC);
		System.out.println("Akaike: " + mayAIC.getAIC());
		System.out.println("---------------------------------------------------");
		generalFunction(mayAIC.getAdj(),pred,ini,alpha,mayAIC.getIndices_adyacencia());
		System.out.println("---------------------------------------------------");
		System.out.println("Mejor modelo segun MDL");
		System.out.println("Orden de las variables: " + orden_MDL);
		System.out.println("MDL: " + mayMDL.getMDL());
		System.out.println("---------------------------------------------------");
		generalFunction(mayMDL.getAdj(),pred,ini,alpha,mayMDL.getIndices_adyacencia());
		System.out.println(cont);
	}

	public static String FILE_NAME = "dataBin7varBinds_NoBinds.arff";
	//public static String FILE_NAME = "dataBin7varBinds_NoBinds.arff";
	//public static String FILE_NAME = "dataBin8varBinds_NoBinds.arff";

	public static void main(String[] args) throws Exception {
		long start_time = System.nanoTime();
		int num_threads = Runtime.getRuntime().availableProcessors();
		double alpha = 1.0;
		IniStats ini = readArff(FILE_NAME);
		int n = ini.getDataset().get(0).length;
		int pred = n-1;
		boolean doFB = false;
		if(n <= 6){
			doFB = true;
		}
		long a = (long) Math.pow(n,2);
		ArrayList<String> file_names = new ArrayList<>();
		File dags0 = new File("dags0.txt");
		boolean exists = dags0.exists();
		if (!exists){
			file_names = writeFiles(num_threads,n,a);
		}
		else{
			for(int i = 0; i < num_threads; i++) {
				String name = "dags" + i + ".txt";
				file_names.add(name);
			}

		}

		ResMulti multi = new ResMulti(num_threads, start_time,pred,ini,alpha, file_names,n,ini.getListData(),ini.getPermNames());
		ArrayList<Thread> threads = new ArrayList<>();
		for(int i = 0; i < num_threads; i++){
			Multithread nm = new Multithread(i,num_threads,"thread "+i, multi, file_names.get(i), n, ini.getDataset(),1,ini.getCard(),pred,doFB);
			threads.add(new Thread(nm));
		}
		for(int i = 0; i < threads.size(); i++){
			threads.get(i).start();
		}
	}
}