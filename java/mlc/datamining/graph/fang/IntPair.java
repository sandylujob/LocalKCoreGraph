package mlc.datamining.graph.fang;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeSet;

public class IntPair implements Comparable{
	int a; int b;
	public IntPair(int aa, int bb){ a = aa; b = bb; }
	public int compareTo(Object obj){
	IntPair ip = (IntPair)obj;
	int diff = a - ip.a;
	if (diff == 0) diff = b - ip.b;
	return diff;
	}
			

public void geneIn(){
	Scanner in = null;
	try {
		in = new Scanner(new File("humanGeneSets.txt"));
		} catch (FileNotFoundException e){
		System.err.println("humanGeneSets not found");
		System.exit(1);
		}
		int[] gids = new int[30];
		TreeSet<IntPair> tset = new TreeSet<IntPair>();
		while (in.hasNextLine()){
			String[] terms = in.nextLine().split(" ");
			int len = terms.length;
			for (int i = 0; i < len; i++)
			gids[i] = Integer.parseInt(terms[i]);
			for (int i = 0; i < len; i++)
			for (int j = 0; j < i; j++){
			tset.add(new IntPair(gids[i], gids[j]));
			tset.add(new IntPair(gids[j], gids[i]));
				}
			}
		in.close();
		maxEdges = tset.size();
		numberOfVertices = numberOfGenes;
		starts = new int[numberOfVertices + 1];
		neighbors = new int[maxEdges];
		Iterator<IntPair> iter = tset.iterator();
		int currVertex = 0;
		starts[0] = 0;
		numberOfNeighbors = 0;
		while (iter.hasNext()){
		IntPair ip = iter.next();
		if (ip.a != currVertex){
		starts[ip.a] = numberOfNeighbors;
		currVertex = ip.a;
		}
		neighbors[numberOfNeighbors++] = ip.b;
		}
		starts[numberOfVertices] = numberOfNeighbors;
		}
}
