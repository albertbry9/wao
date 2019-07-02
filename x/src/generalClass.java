import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class generalClass{
	private double aux;
	private ArrayList<Double> aux2;
	private ArrayList<int[]> auxSTRING;
	private ArrayList<Double> auxCount;
	public generalClass(){
		this.aux = 0.0;
		this.aux2 = new ArrayList<>();
		this.auxSTRING = new ArrayList<>();
		this.auxCount = new ArrayList<>();
	}
	public HashMap<Integer,Integer>[] Cardinality(ArrayList<int[]> dataset){
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
	}
	public int Count (ArrayList<int[]> dataset, int[] conditions){
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
	public Refact.res_marg_conj marginal_conjoint_probability(ArrayList<int[]> dataset, int[] conditions, double alpha, HashMap<Integer,Integer>[] new_cardinality, boolean isVerif){
		double number_instances = dataset.size();
		if(number_instances == 0){
			Refact.res_marg_conj res = new Refact.res_marg_conj(0,0);
			return res;
		}
		List<Integer> aux = new ArrayList<>();
		for (int i = 0; i < conditions.length;i++) {
			if(conditions[i] == -1)
				continue;
			aux.add(i);
		}
		double multi_cards = 1;
		double count = Count(dataset,conditions);
		HashMap<Integer,Integer>[] cardinality;
		if(isVerif){
			cardinality = new_cardinality;
		}else{
			cardinality = Cardinality(dataset);
		}
		double[] number_cardinality = get_cards_dimension(cardinality);
		for(int i = 0; i < aux.size();i++){
			multi_cards *= number_cardinality[aux.get(i)];
		}
		Refact.res_marg_conj res = new Refact.res_marg_conj(((count+alpha)/(number_instances+(multi_cards*alpha))),count);
		return res;
	}
	public Refact.res_marg_conj conditional_probability(ArrayList<int[]> dataset, int[] condition1, int[] condition2, double alpha, HashMap<Integer,Integer>[] new_cardinality, boolean isVerif){
		Refact.res_marg_conj dist1 = this.marginal_conjoint_probability(dataset,condition1,alpha, new_cardinality, isVerif);
		Refact.res_marg_conj dist2 = this.marginal_conjoint_probability(dataset,condition2,alpha, new_cardinality, isVerif);
		Refact.res_marg_conj res = new Refact.res_marg_conj(dist2.getRes()/dist1.getRes(),dist2.getCount());
		return res;
	}
	public Refact.res_graph_dist graph_distributions(ArrayList<int[]> dataset, int[] cols, double alpha, int idx, ArrayList<Integer>pcc, ArrayList<Double> almacen, ArrayList<Double> almacenCount, HashMap<Integer,Integer>[] cards, HashMap<Integer,Integer>[] new_cardinality, boolean isVerif){
		double[] num_cards;
		if(isVerif){
			num_cards = this.get_cards_dimension(new_cardinality);
		}
		else{
			num_cards = this.get_cards_dimension(cards);
		}
		if (idx==cols.length){
			int[] arr1 = new int[num_cards.length];
			int[] arr2;
			Arrays.fill(arr1,-1);
			for (int i = 0;i<pcc.size();i++ ){
				arr1[cols[i]] = pcc.get(i);
			}
			arr2 = arr1.clone();
			arr2[cols[cols.length-1]] = -1;
			Refact.res_marg_conj sum = this.conditional_probability(dataset,arr2,arr1,alpha, new_cardinality, isVerif);
			aux+=sum.getRes();
			aux2.add(sum.getRes());
			auxCount.add(sum.getCount());
			auxSTRING.add(arr1);
			if(aux2.size()==num_cards[cols[cols.length-1]]){
				double aux3 = 0;
				for(int k = 0; k<aux2.size();k++){
					aux3 += aux2.get(k)/aux;
					almacen.add(aux2.get(k)/aux);
					almacenCount.add(auxCount.get(k)*log2(aux2.get(k)/aux));
					//System.out.println("P( " + Arrays.toString(auxSTRING.get(k))+ " )= " +aux2.get(k)/aux+"   "+((char)(65+cols[cols.length-1]))+ " es hijo.");
				}
				//System.out.println("Suma de probabilidades: "+aux3);
				//System.out.println("");
				aux2 = new ArrayList<>();
				auxCount = new ArrayList<>();
				auxSTRING = new ArrayList<>();
				aux = 0.0;
			}
			Refact.res_graph_dist res = new Refact.res_graph_dist(almacen,almacenCount);
			return res;
		}

		double c = num_cards[cols[idx]];

		for(int i = 0; i< c;i++){
			pcc.add(i);
			this.graph_distributions(dataset,cols,alpha,idx+1,pcc,almacen, almacenCount,cards, new_cardinality, isVerif);
			pcc.remove(idx);
		}
		Refact.res_graph_dist res = new Refact.res_graph_dist(almacen,almacenCount);
		return res;
	}
	public double[] get_cards_dimension(HashMap<Integer, Integer>[] hm){
		double[] cards = new double[hm.length];
		for(int i = 0; i < hm.length; i++)
			cards[i] = (double) hm[i].size();
		return cards;
	}
	public Refact.res_factor getFactors(ArrayList<int[]> dataset, double alpha, ArrayList<int[]> indices_adyacencia, HashMap<Integer,Integer>[] cards, HashMap<Integer,Integer>[] new_cardinality, boolean isVerif){
		ArrayList<ArrayList<Double>> gran_almacen = new ArrayList<>();
		ArrayList<ArrayList<Double>> gran_count = new ArrayList<>();
		for(int i = 0; i < indices_adyacencia.size();i++) {
			ArrayList<Integer> pcc = new ArrayList<>();
			ArrayList<Double> almacen = new ArrayList<>();
			ArrayList<Double> count = new ArrayList<>();
			Refact.res_graph_dist res = this.graph_distributions(dataset,indices_adyacencia.get(i),alpha,0,pcc,almacen, count,cards,new_cardinality,isVerif);
			gran_almacen.add(res.getAlmacen());
			gran_count.add(res.getAlmacenCount());
		}
		Refact.res_factor res = new Refact.res_factor(gran_almacen,gran_count);
		return res;
	}
	public double log2(double d){
		return Math.log(d)/Math.log(2.0);
	}
	public int[][] strToAdj(String s, int n){
		int[][] ret = new int[n][n];
		String[] mtxStr = s.split("");
		for(int i = 0; i < mtxStr.length; i++){
			ret[i/n][i%n] = Integer.parseInt(mtxStr[i]);
		}
		return ret;
	}
	public double parametrosCalculables(ArrayList<int[]> indices_adyacencia,ArrayList<int[]> dataset)
	{
		HashMap<Integer,Integer>[] card_original = this.Cardinality(dataset);
		double[] cards = this.get_cards_dimension(card_original);
		double k = 0;
		int aux = 1;
		for(int i = 0; i < indices_adyacencia.size(); i++)
		{
			if(indices_adyacencia.get(i).length == 1){
				k+=(cards[i]-1);
			}
			else
			{
				for (int j = 0; j < indices_adyacencia.get(i).length - 1;j++)
				{
					aux*=cards[indices_adyacencia.get(i)[j]];
				}
				k+=(cards[i]-1)*aux;
			}
			aux = 1;
		}
		return k;
	}
	public double getEntropy(ArrayList<ArrayList<Double>> gran_count){
		double res = 0.0;
		for(int i = 0; i < gran_count.size(); i++){
			for(int j = 0; j < gran_count.get(i).size(); j++){
				res+=gran_count.get(i).get(j);
			}
		}
		return -1.0*res;
	}
	public Refact.res_comparison forComparison(int[][] adj, ArrayList<int[]> indices_adyacencia, ArrayList<int[]> dataset, double alpha, HashMap<Integer,Integer>[] cards){
		Refact.res_factor res = this.getFactors(dataset,alpha,indices_adyacencia,new HashMap[0],cards,true);
		double k = this.parametrosCalculables(indices_adyacencia,dataset);
		double H = this.getEntropy(res.getGran_count());
		double AIC = H+k;
		double MDL = H+(this.log2(dataset.size())*(k/2));
		//System.out.println(H + " " + AIC + " " + MDL);
		Refact.res_comparison ret = new Refact.res_comparison(H,AIC,MDL,adj,indices_adyacencia);
		return ret;
	}
	public ArrayList<int[]> read_matrix(int[][] matrix){
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
	public ArrayList<int[]> InstancesToArray(Instances data){
		ArrayList<int[]> arffData = new ArrayList<>();
		for (int i = 0; i < data.numAttributes(); ++i) {
			double[] values = data.attributeToDoubleArray(i);
			int[]v = new int[values.length];
			for (int j = 0; j < values.length;++j) {
				v[j] = (int)values[j];
			}
			arffData.add(v);
		}
		int[][] aux = new int[arffData.size()][];
		for(int i = 0; i < arffData.size(); ++i){
			int[] row = arffData.get(i);
			aux[i] = row;
		}
		int[][]tAux = new int[aux[0].length][aux.length];
		for (int i = 0; i < aux.length ;++i){
			for(int j = 0; j <aux[0].length;++j){
				tAux[j][i] = aux[i][j];
			}
		}
		ArrayList<int[]> dataset = new ArrayList<>(Arrays.asList(tAux));
		return dataset;
	}
	public void printPermutn(String str, String ans, Instances data, ArrayList<String> permNames) throws Exception {
		if (str.length() == 0) {
			ans = ans.replaceAll("",",");
			permNames.add(ans.substring(1, ans.length()-1));
			return;
		}

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			String ros = str.substring(0, i) + str.substring(i + 1);
			printPermutn(ros, ans + ch, data,permNames);
		}
	}
}