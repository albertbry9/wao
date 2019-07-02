import java.util.ArrayList;

public class Permutacion{
	private ArrayList<int[]> vlr_vrb;
	private ArrayList<Integer> position;

	public Permutacion(){
		this.vlr_vrb = new ArrayList<>();
		this.position = new ArrayList<>();
	}

	public void add_att(int[] vlr){
		this.vlr_vrb.add(vlr);
		this.position.add(0);
	}

	public void endPermute(){
		for(int i = 0; i < this.position.size(); i++){
			this.position.set(i,0);
		}
	}

	public int[] permutar(){
		if(this.position == null){
			return null;
		}
		int[] ret = new int[this.vlr_vrb.size()];
		for(int i = 0; i < this.vlr_vrb.size(); i++){
			ret[i] = this.vlr_vrb.get(i)[this.position.get(i)];
		}
		for(int i = 0; i < this.vlr_vrb.size(); i++){
			this.position.set(i,this.position.get(i)+1);
			if(this.position.get(i) >= this.vlr_vrb.get(i).length){
				if(i == this.position.size()-1){
					this.position = null;
					return ret;
				}
				this.position.set(i,0);
			}else{
				break;
			}
		}
		return ret;
	}
}