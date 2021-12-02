package genetic_algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.DoubleStream;
import java.lang.Math.*;

public class GeneticAlgorithm {
	private static final int VEHICLE_WEIGHT_LIMIT = 500;
	private static final int POPULATION_SIZE = 1000;
	private static final int GENERATIONS = 5000;
	
	private Item[] itemList;
	private boolean[][] population;
	private int[] selections;
	private int[] matingPairs;
	private Random rand = new Random();;
	private double[] fitness;
	private double[] probDistribution;
	private double totalWeight;
	private Item[] selectedItems;	
	private Scanner readFile;
	private Scanner scanSize;
	private FileWriter report;
	private int listSize;
	private double[] aveFitnessQueue;
	private double maxFitness;
	
	public void geneticAlgorithm() throws IOException {
		readFile = new Scanner(new File("Input.txt"));
		itemList = new Item[getListSize()];
		population = new boolean[POPULATION_SIZE][itemList.length];
		fitness = new double[POPULATION_SIZE];
		probDistribution = new double[POPULATION_SIZE];
		selections = new int[POPULATION_SIZE];
		matingPairs = new int[POPULATION_SIZE];
		report = new FileWriter("Output.txt");
		aveFitnessQueue = new double[10];
		maxFitness = 0;
		
		readFile();
		
		initPopulation();
		
		calcFitness();
		
		calcProbDistribution();
		
		for(int i = 0; i < GENERATIONS; i++) {
			writeGenerationalReport(i + 1, getAverageFitness());
			
			artificialSelection();
			
			selectMates();
			
			crossover();
			
			mutation();
			
			calcFitness();
			
			calcProbDistribution();
			
			getMaxFitness();
			
			if(improvementPlateau() == true) {
				break;
			}
		}
		
		finalSelectedItems();
		
		writeFinalReport(POPULATION_SIZE, getMaxFitness(), GENERATIONS, selectedItems, getTotalUtility());
	}
	
	public void initPopulation() throws FileNotFoundException {		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			for(int j = 0; j < itemList.length; j++) {
				if(rand.nextInt(25) == 1) {
					population[i][j] = true;
				}else {
					population[i][j] = false;
				}
			}
		}	
	}
	
	public void calcFitness() {
		for(int i = 0; i < POPULATION_SIZE; i++) {
			fitness[i] = 0;
		}
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			for(int j = 0; j < itemList.length; j++) {
				if(population[i][j] == true) {
					fitness[i] += itemList[j].getUtility();
					totalWeight += itemList[j].getWeight();
				}
			}
			if(totalWeight > VEHICLE_WEIGHT_LIMIT) {
				fitness[i] = 1;
			}
			totalWeight = 0;
		}
	}
	
	public void calcProbDistribution() {
		double y2;
		double sum = 0;
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			sum += (fitness[i] * fitness[i]);			
		}
		
		for(int i = 0; i < POPULATION_SIZE; i++) {			
			probDistribution[i] = ((fitness[i] * fitness[i]) / sum);
		}
	}
	
	public double getMaxFitness() {
		double max = fitness[0];
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			if(fitness[i] > max) {
				max = fitness[i];
			}
		}
		
		if(max > maxFitness) {
			maxFitness = max;
		}
		
		return maxFitness;
	}
	
	public double getAverageFitness() {
		double total = fitness[0];
		for(int i = 1; i < POPULATION_SIZE; i++) {
			total += fitness[i];
		}
		
		return total / POPULATION_SIZE;
	}
	
	public double getTotalUtility() {
		double utility = 0;
		for(int i = 0; i < selectedItems.length; i++) {
			utility += selectedItems[i].getUtility();
		}
		
		return utility;
	}
	
	public void artificialSelection() {
		double select;
		double test;
		int index = 0;
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			selections[i] = 0;
		}
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			select = rand.nextDouble();
			test = 0;

			for(int j = 0; j < POPULATION_SIZE; j++) {
				test += probDistribution[j];
				if(select < test) {
					selections[j]++;
					break;
				}
			}
		}
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			if(selections[i] > 0) {
				for(int j = 0; j < selections[i]; j++) {
					population[index] = population[i];
					index++;
				}
			}
		}
	}
	
	public void selectMates() {
		int mate;
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			matingPairs[i] = -1;
		}
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			mate = rand.nextInt(POPULATION_SIZE);
			if(matingPairs[i] < 0) {
				matingPairs[i] = mate;
				matingPairs[mate] = i;
			}
		}
	}
	
	public void crossover() {
		int crossPoint;
		int pair;
		boolean temp;
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			crossPoint = rand.nextInt(itemList.length);
			pair = matingPairs[i];
			
			if(pair > -1) {
				for(int j = 0; i < itemList.length; i++) {
					if(j > crossPoint) {
						temp = population[i][j];
						population[i][j] = population[pair][j];
						population[pair][j] = temp;
					}
				}
				matingPairs[i] = -1;
				matingPairs[pair] = -1;
			}
		}
	}
	
	public void mutation() {
		for(int i = 0; i < POPULATION_SIZE; i++) {
			for(int j = 0; j < itemList.length; j++) {
				if(rand.nextInt(1000000) == 42) {
					if(population[i][j] == true) {
						population[i][j] = false;
					}else if(population[i][j] == false) {
						population[i][j] = true;
					}
				}
			}
		}
	}
	
	public boolean improvementPlateau() {
		double target;
		
		for(int i = 9; i > 0; i--) {
			aveFitnessQueue[i] = aveFitnessQueue[i-1];
		}
		
		aveFitnessQueue[0] = getAverageFitness();
		
		target = (aveFitnessQueue[9] * .01) + aveFitnessQueue[9];
		
		if(aveFitnessQueue[0] < target) {
			return true;
		}else {
			return false;
		}
	}
	
	public void finalSelectedItems() {
		int index = 0;
		int count = 0;
		int index2 = 0;
		
		for(int i = 0; i < POPULATION_SIZE; i ++) {
			if(fitness[i] == getMaxFitness()) {
				index = i;
			}
		}
		
		for(int i = 0; i < itemList.length; i ++) {
			if(population[index][i] == true) {
				count++;
			}
		}
		
		selectedItems = new Item[count];
		
		for(int i = 0; i < itemList.length; i ++) {
			if(population[index][i] == true) {
				selectedItems[index2] = itemList[i];
				index2++;
			}
		}
	}
	
	
	public void readFile() throws FileNotFoundException{
		readFile = new Scanner(new File("Input.txt"));
	
		int index = 0;
		
		while(readFile.hasNextDouble()) {
			Item item = new Item(readFile.nextDouble(), readFile.nextDouble());
			itemList[index] = item;
			index++;
		}
	}
	
	public int getListSize() throws FileNotFoundException {
		scanSize = new Scanner(new File("Input.txt"));
		
		while(scanSize.hasNextDouble()) {
			scanSize.nextDouble();
			scanSize.nextDouble();
			listSize++;
		}
		
		return listSize;
	}
	
	public void writeGenerationalReport(int generation, double averageFitness) throws IOException {
		report.write("Generation: " + generation + "\nAverage Fitness: " + averageFitness + "\n\n");
		
	}
	
	public void writeFinalReport(int startingPopulation, double maxFitness, int generations, Item[] selectedItems, double totalUtility) throws IOException {
		report.write("Starting Population: " + startingPopulation + "\nMax fitness after " + generations + " generations: " + maxFitness + "\nItems taken:");
		
		for(int i = 0; i < selectedItems.length; i++) {
			report.write("\nutility: " + selectedItems[i].getUtility() + ", weight: " + selectedItems[i].getWeight());			
		}
		
		report.write("\nTotal utility: " + totalUtility);
		
		report.close();
	}
	
}
