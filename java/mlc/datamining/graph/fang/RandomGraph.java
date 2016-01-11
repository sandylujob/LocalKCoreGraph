
package mlc.datamining.graph.fang;
import java.io.*;
import java.util.*;

public class RandomGraph{
	int numberOfVertices = 0;
	double onRate = 0;
	int[] starts = null;
	int[] neighbors = null;
	int maxEdges = 0;
	int numberOfNeighbors = 0;
	
	int[] comp = null;
	
	public RandomGraph(int n, double p){
	numberOfVertices = n;
	onRate = p;
	maxEdges = (int)(1.2 * p * n * n);
	starts = new int[numberOfVertices + 1];
	neighbors = new int[maxEdges];
	Random random = new Random();
	for (int i = 0; i < numberOfVertices; i++){
	starts[i] = numberOfNeighbors;
	for (int j = 0; j < i; j++){
	int k = starts[j]; int up = starts[j + 1];
	for (; k < up; k++) if (neighbors[k] == i) break;
	if (k < up) neighbors[numberOfNeighbors++] = j;
	}
	for (int j = i + 1; j < numberOfVertices; j++)
	if (random.nextDouble() < onRate)
	neighbors[numberOfNeighbors++] = j;
	}
	starts[numberOfVertices] = numberOfNeighbors;
	}

	public void dfs(int index, int label){
		comp[index] = label;
		int up = starts[index + 1];
		for (int i = starts[index]; i < up; i++){
		int k = neighbors[i];
		if (comp[k] == -1) dfs(k, label);
			}
		}
	
	public void components(){
		comp = new int[numberOfVertices];
		for (int i = 0; i < numberOfVertices; i++) comp[i] = -1;
		for (int i = 0; i < numberOfVertices; i++)
			if (comp[i] == -1) dfs(i, i);
		int[] componentSize = new int[numberOfVertices];
		for (int i = 0; i < numberOfVertices; i++)
		componentSize[i] = 0;
		for (int i = 0; i < numberOfVertices; i++)
		componentSize[comp[i]]++;
		int maxSize = 0; int second = 0;
		for (int i = 0; i < numberOfVertices; i++)
		if (componentSize[i] > maxSize){
		second = maxSize; maxSize = componentSize[i];
		}else if (componentSize[i] > second)
		second = componentSize[i];
		System.out.println(maxSize + " " + second);
		}
	
	public int disconnectivity(int v){
			comp = new int[numberOfVertices];
			for (int i = 0; i < numberOfVertices; i++) comp[i] = -2;
			for (int i = starts[v]; i < starts[v + 1]; i++)
			comp[neighbors[i]] = -1;
			for (int i = 0; i < numberOfVertices; i++)
			if (comp[i] == -1) dfs(i, i);
			int numberOfComponents = 0;
			for (int i = 0; i < numberOfVertices; i++)
			if (comp[i] == i) numberOfComponents++;
			return numberOfComponents;
			}
	
	public int topNode(){
		int maxDegree = 0; int index = -1;
		for (int i = 0; i < numberOfVertices; i++){
		int degree = starts[i + 1] - starts[i];
		if (degree > maxDegree){
		maxDegree = degree; index = i;
								}
		}
			return index;
	}
	
		public static void main(String[] args){
		if (args.length < 2){
		System.err.println("Usage: java RandomGraph n p");
		return;
		}
		RandomGraph rg = new RandomGraph(Integer.parseInt(args[0]),
		Double.parseDouble(args[1]));
		rg.components();
		int topVertex = rg.topNode();
		System.out.println(rg.disconnectivity(topVertex));
		}
		

}


