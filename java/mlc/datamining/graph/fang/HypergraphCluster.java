package mlc.datamining.graph.fang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import mlc.datamining.util.NameIndexPair;
import mlc.datamining.util.NeighborNode;
//import mlc.text.ShredWordCount;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class HypergraphCluster {

	public static final DecimalFormat numberFormat = new DecimalFormat("0.##");

	UndirectedSparseGraph<Number, Number> network = new UndirectedSparseGraph();
	ArrayList<Number> vertexList; 
	ArrayList<Number> edgeList; 
	BetweennessCentrality rankerBetweenness;
	
	NameIndexPair fangNameIndex = new NameIndexPair();
	NameIndexPair yaoNameIndex = new NameIndexPair();
	HashMap<Integer, HashSet<Integer>> fangYao = new HashMap<Integer, HashSet<Integer>>();
	
	String workingDir = "";
	String fileFangYao = "";
	String fileFangCode = "codeFang.txt";
	String fileYaoCode = "codeYao.txt";
	String fileBetweenness = "betweenness.txt";
	
	Comparator numberComparator = new Comparator<Number>() {
	    public int compare(Number o1, Number o2) {
	    	return o1.intValue() - o2.intValue();
	    }};
	
	private void readData() throws Exception {
    	File file = new File(workingDir+fileFangYao);
		if(file.exists()) {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			String line = null; 
			int indexFang = 0;
			int indexYao = 0;
			while ((line = input.readLine()) != null){
				//input format: fang: yao1 yao2 ....
				StringTokenizer st = new StringTokenizer(line, ":");
				if(st.hasMoreTokens()) {
					String fang = st.nextToken();
					if(st.hasMoreTokens()) {
						fangNameIndex.add(indexFang, fang);
						HashSet<Integer> yaoList = new HashSet<Integer>();
						fangYao.put(indexFang, yaoList);
						StringTokenizer stYao = new StringTokenizer(st.nextToken(), " ");
						while(stYao.hasMoreTokens()) {
							String yao = stYao.nextToken();
							Integer index = yaoNameIndex.getIndexByName(yao);
							if(index==null) {
								yaoList.add(indexYao);
								yaoNameIndex.add(indexYao++, yao);
							} else {
								yaoList.add(index);
							}
						}
						indexFang++;
					}
				}
			}
	    	input.close();
		}

		fangNameIndex.print(workingDir+fileFangCode);
		yaoNameIndex.print(workingDir+fileYaoCode);
		
	}

	private void buildNetwork() {
		for(int i=0; i<fangYao.size(); i++) {
			HashSet<Integer> yaoList1 = fangYao.get(i);
			for(int j=0; j<i; j++) {
				HashSet<Integer> yaoCombine = new HashSet<Integer>();
				HashSet<Integer> yaoList2 = fangYao.get(j);
				yaoCombine.addAll(yaoList1);
				yaoCombine.addAll(yaoList2);
				if(yaoList1.size() + yaoList2.size() > yaoCombine.size()) {	//has common
					network.addEdge(network.getEdgeCount()+1, i, j, EdgeType.UNDIRECTED);
				}
			}
		}

		refreshNetwork();
		
	}

	public void getBetweenness() throws Exception {
		rankerBetweenness = new BetweennessCentrality(network, true, true);
		rankerBetweenness.setRemoveRankScoresOnFinalize(false);
		rankerBetweenness.evaluate();
//		ranker.printRankings(true, true);

		StringBuilder sb = new StringBuilder();
		sb.append("vertex:\n");
		for(Number vertex : vertexList) {
			double score = rankerBetweenness.getVertexRankScore(vertex);
			sb.append(vertex + "\t" + numberFormat.format(score) + "\n");
		}
		
		int numOfEdge = edgeList.size();
		double[] betweennessE = new double[numOfEdge];
		sb.append("\nedges:\n");
		for(Number edge : edgeList) {
			int i = edge.intValue() - 1;
			betweennessE[i] = rankerBetweenness.getEdgeRankScore(edge);
			sb.append(edge + "\t" + numberFormat.format(betweennessE[i]) + "\t" + network.getEndpoints(edge) + "\n");
		}
		
		FileWriter out = new FileWriter(new File(workingDir+fileBetweenness));
		out.write(sb.toString());
		out.close();
	}

	public void removeByBetweenness() throws Exception {
		Number edge = getHighestBetweennessEdge();
		network.removeEdge(edge);
		refreshNetwork();
	}
	
	public Number getHighestBetweennessEdge() throws Exception {
		rankerBetweenness = new BetweennessCentrality(network, true, true);
		rankerBetweenness.setRemoveRankScoresOnFinalize(false);
		rankerBetweenness.evaluate();
//		ranker.printRankings(true, true);

		double highestScore = 0.0;
		Number highestIndex = 0;
		for(Number edge : edgeList) {
			if(highestScore < rankerBetweenness.getEdgeRankScore(edge)) {
				highestScore = rankerBetweenness.getEdgeRankScore(edge);
				highestIndex = edge;
			}
		}
		
		System.out.println("Highest betweenness edge: " + highestIndex);
		StringBuilder sb = new StringBuilder();
		Pair<Number> vertexPair = network.getEndpoints(highestIndex);
		int fang1 = vertexPair.getFirst().intValue();
		HashSet<Integer> yaoList1 = fangYao.get(fang1);
		sb.append("1, " + fangNameIndex.getNameByIndex(fang1) + ": ");
		for(int i : yaoList1) {
			sb.append(yaoNameIndex.getNameByIndex(i)).append(",");
		}
		sb.append("\n");
		int fang2 = vertexPair.getSecond().intValue();
		HashSet<Integer> yaoList2 = fangYao.get(fang2);
		sb.append("2, " + fangNameIndex.getNameByIndex(fang2) + ": ");
		for(int i : yaoList2) {
			sb.append(yaoNameIndex.getNameByIndex(i)).append(",");
		}
		sb.append("\nIntersection: ");
		HashSet<Integer> yaoListIntersection = new HashSet<Integer>();
		yaoListIntersection.addAll(yaoList1);
		yaoListIntersection.retainAll(yaoList2);
		for(int i : yaoListIntersection) {
			sb.append(yaoNameIndex.getNameByIndex(i)).append(",");
		}
		System.out.println(sb.toString());
		
		return highestIndex;
	}

	private void refreshNetwork() {
		vertexList = new ArrayList<Number>(network.getVertices());
		Collections.sort(vertexList, numberComparator);
		int vertexCount = vertexList.size();
		System.out.println("vertex count: " + vertexCount);
		
		edgeList = new ArrayList<Number>(network.getEdges());
		Collections.sort(edgeList,numberComparator);
		int edgeCount = edgeList.size();
		System.out.println("edge count: " + edgeCount + "/" + (vertexCount*(vertexCount-1)/2) 
				+ "=" + numberFormat.format(edgeCount*2.0/(vertexCount*(vertexCount-1))));
	}

	private void getDegrees() {
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, new Comparator<Number>() {
		    public int compare(Number o1, Number o2) {
		    	return network.getNeighbors(o2).size() - network.getNeighbors(o1).size();
		    }});


		for(Number vertex : vertexArr) {
			int index = vertex.intValue();
			System.out.println(fangNameIndex.getNameByIndex(index) + ": " + vertex + "\t" + network.getNeighbors(vertex).size());
		}
		
	}

	
	public static void main(String[] args) throws Exception 
	{
		System.out.println(new Date());

		HypergraphCluster hgCluster = new HypergraphCluster();
		hgCluster.workingDir = "C:/data/dev/java/tcm/data/text/hypergraph/";
		hgCluster.fileFangYao = "FangYao.txt";
		
		hgCluster.readData();
		hgCluster.buildNetwork();
		hgCluster.getDegrees();
//		hgCluster.getBetweenness();		
		for(int i=0; i<10; i++) {
			hgCluster.removeByBetweenness();
		}
		
		System.out.println(new Date());
	}





}
