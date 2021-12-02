package genetic_algorithms;

public class Item {
	private double utility;
	private double weight;
	
	public Item(double utility, double weight) {
		this.utility = utility;
		this.weight = weight;
	}
	
	public double getUtility() {
		return utility;
	}
	
	public double getWeight() {
		return weight;
	}

}
