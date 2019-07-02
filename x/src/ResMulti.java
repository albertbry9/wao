import java.io.IOException;
import java.util.ArrayList;

public class ResMulti{
	private long start_time;
	private boolean[] isFinished;
	private int pred;
	private int n;
	private double alpha;
	private ArrayList<Refact.res_comparison> multH;
	private ArrayList<Refact.res_comparison> multMDL;
	private ArrayList<Refact.res_comparison> multAIC;
	private ArrayList<String> file_names;
	private ArrayList<ArrayList<int[]>> list_datasets;
	private ArrayList<String> permNames;
	private IniStats ini;
	private boolean doFB;
	public ResMulti(int number_threads, long start_time, int pred, IniStats ini, double alpha, ArrayList<String> file_names, int n, ArrayList<ArrayList<int[]>> list_datasets, ArrayList<String> permNames){
		this.start_time = start_time;
		this.isFinished = new boolean[number_threads];
		this.multH = new ArrayList<>();
		this.multMDL = new ArrayList<>();
		this.multAIC = new ArrayList<>();
		this.pred = pred;
		this.n = n;
		this.ini = ini;
		this.alpha = alpha;
		this.file_names = file_names;
		this.list_datasets = list_datasets;
		this.permNames = permNames;
	}
	public void endThread(int id, Refact.res_comparison H, Refact.res_comparison MDL,Refact.res_comparison AIC,boolean doFB) throws Exception {
		this.doFB = doFB;
		this.isFinished[id] = true;
		this.multH.add(H);
		this.multMDL.add(MDL);
		this.multAIC.add(AIC);
		boolean allFinished = true;
		for (boolean is: this.isFinished) {
			if(!is){
				allFinished = false;
				break;
			}
		}
		if(allFinished){
			endAll();
		}
	}
	public void endAll() throws Exception {
		if(this.doFB){
			double mayH = -99999;
			double mayAIC = -99999;
			double mayMDL = -99999;
			Refact.res_comparison mayHObj = null;
			Refact.res_comparison mayAICObj = null;
			Refact.res_comparison mayMDLObj = null;
			for(int i = 0; i < this.multH.size(); i++){
				if(multH.get(i).getH()>mayH){
					mayH = multH.get(i).getH();
					mayHObj = multH.get(i);
				}
				if(multAIC.get(i).getAIC()>mayAIC){
					mayAIC = multAIC.get(i).getAIC();
					mayAICObj = multAIC.get(i);
				}
				if(multMDL.get(i).getMDL()>mayMDL){
					mayMDL = multMDL.get(i).getMDL();
					mayMDLObj = multMDL.get(i);
				}
			}
			System.out.println("===================================================");
			System.out.println("Resultados para Fuerza Bruta");
			System.out.println("---------------------------------------------------");
			System.out.println("Mejor modelo segun Entropia");
			System.out.println("H: " + mayHObj.getH());
			System.out.println("---------------------------------------------------");
			Refact.generalFunction(mayHObj.getAdj(),this.pred,this.ini,this.alpha,mayHObj.getIndices_adyacencia());
			System.out.println("---------------------------------------------------");
			System.out.println("Mejor modelo segun Akaike");
			System.out.println("Akaike: " + mayAICObj.getAIC());
			System.out.println("---------------------------------------------------");
			Refact.generalFunction(mayAICObj.getAdj(),this.pred,this.ini,this.alpha,mayAICObj.getIndices_adyacencia());
			System.out.println("---------------------------------------------------");
			System.out.println("Mejor modelo segun MDL");
			System.out.println("MDL: " + mayMDLObj.getMDL());
			System.out.println("---------------------------------------------------");
			Refact.generalFunction(mayMDLObj.getAdj(),this.pred,this.ini,this.alpha,mayMDLObj.getIndices_adyacencia());
		}
		Refact.k2NotPermutation(this.n,this.ini,this.alpha,this.pred,true,new ArrayList<>());
		System.out.println("====================================================");
		Refact.k2Permutation(this.n,this.ini,this.alpha,this.pred,this.permNames);
		long finish_time = System.nanoTime() - this.start_time;
		System.out.println("Tiempo transcurrido: " + ((double) finish_time / 1_000_000_000.0) + "s");
		System.out.println("Finalizo todo");
	}
}
