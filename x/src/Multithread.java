import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Multithread implements Runnable{
	private int number_threads;
	private int n;
	private int id;
	private int pred;
	private double alpha;
	private String nombre;
	private String path;
	private ResMulti multi;
	private double aux;
	private ArrayList<Double> aux2;
	private ArrayList<Double> auxCount;
	private ArrayList<int[]> auxSTRING;
	private ArrayList<int[]> dataset;
	private HashMap<Integer,Integer>[] card;
	private generalClass sameFunctions;
	private boolean doFB;
	public Multithread(int id, int number_threads, String nombre, ResMulti multi, String path, int n, ArrayList<int[]> dataset, double alpha, HashMap<Integer,Integer>[] card, int pred, boolean doFB){
		this.id = id;
		this.n = n;
		this.number_threads = number_threads;
		this.nombre = nombre;
		this.path = path;
		this.multi = multi;
		this.alpha = alpha;
		this.aux = 0.0;
		this.aux2 = new ArrayList<>();
		this.auxCount = new ArrayList<>();
		this.auxSTRING = new ArrayList<>();
		this.dataset = dataset;
		this.card = card;
		this.pred = pred;
		this.sameFunctions = new generalClass();
		this.doFB = doFB;
	}

	@Override
	public void run() {
		double mayH = -99999;
		double mayAIC = -99999;
		double mayMDL = -99999;
		Refact.res_comparison mayHObj = null;
		Refact.res_comparison mayAICObj = null;
		Refact.res_comparison mayMDLObj = null;
		if(this.doFB){
			FileReader fr = null;
			try {
				fr = new FileReader(this.path);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Scanner e = new Scanner(fr);
			while(e.hasNextLine()){
				int[][] adj = this.sameFunctions.strToAdj(e.nextLine(),this.n);
				ArrayList<int[]> indices_adyacencia = this.sameFunctions.read_matrix(adj);
				Refact.res_comparison tmp = this.sameFunctions.forComparison(adj,indices_adyacencia,this.dataset,this.alpha,this.card);
				//System.out.println(tmp.getH() + " "+this.nombre + " H");
				//System.out.println(tmp.getAIC() + " "+this.nombre + " AIC");
				//System.out.println(tmp.getMDL() + " "+this.nombre + " MDL");
				if(tmp.getH()> mayH){

					mayH = tmp.getH();
					mayHObj = tmp;
				}
				if(tmp.getAIC()> mayAIC){

					mayAIC = tmp.getAIC();
					mayAICObj = tmp;
				}
				if(tmp.getMDL()> mayMDL){

					mayMDL = tmp.getMDL();
					mayMDLObj = tmp;
				}
			}
		}
		try {
			this.multi.endThread(id,mayHObj,mayMDLObj,mayAICObj,this.doFB);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}