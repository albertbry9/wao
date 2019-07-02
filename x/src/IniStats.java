import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;

public class IniStats{
	private int num_var;
	private int num_inst;
	private HashMap<Integer,Integer>[] card;
	private ArrayList<ArrayList<int[]>> folds;
	private ArrayList<int[]> dataset;
	private ArrayList<ArrayList<int[]>> list_dataset;
	private ArrayList<String> permNames;
	private Instances data;
	IniStats(int num_var, int num_inst , ArrayList<ArrayList<int[]>> folds, HashMap<Integer,Integer>[] card, ArrayList<int[]> dataset){
		this.num_var = num_var;
		this.num_inst = num_inst;
		this.card = card;
		this.folds = folds;
		this.dataset = dataset;
	}
	public int getNum_var(){
		return this.num_var;
	}
	public int getNum_inst(){
		return this.num_inst;
	}
	public HashMap<Integer, Integer>[] getCard() {
		return this.card;
	}
	public ArrayList<ArrayList<int[]>> getFolds(){
		return this.folds;
	}
	public ArrayList<int[]> getDataset() {
		return this.dataset;
	}
	public void addListData(ArrayList<ArrayList<int[]>> list){
		this.list_dataset = list;
	}
	public ArrayList<ArrayList<int[]>> getList_dataset(){
		return this.list_dataset;
	}
	public ArrayList<ArrayList<int[]>> getListData(){
		return this.list_dataset;
	}
	public void addPermNames(ArrayList<String> permNames){
		this.permNames = permNames;
	}
	public ArrayList<String> getPermNames(){
		return this.permNames;
	}
	public void addData(Instances data){
		this.data = data;
	}
	public Instances getData(){
		return this.data;
	}
}