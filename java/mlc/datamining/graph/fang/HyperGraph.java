package mlc.datamining.graph.fang;

import jama.math.EigenvalueDecomposition;
import jama.math.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import mlc.datamining.util.NameIndexPair;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class HyperGraph extends BaseGraph {

	NameIndexPair fangNameIndex = new NameIndexPair();
	HashMap<Integer, HashSet<Integer>> fangYao = new HashMap<Integer, HashSet<Integer>>();
	
	String fileFangCode = "codeFang.txt";
	String fileYaoCode = "codeYao.txt";
	String fileBetweenness = "betweenness.txt";
	
	public HyperGraph() {
		super();
	}

	public HyperGraph(String dir, String file) throws Exception {
		this.workingDir = dir;
		this.inputFile = file;
		readData();
		buildHyperNetwork();
	}

    public void rebuild() throws Exception {
		network = new UndirectedSparseGraph();
		buildHyperNetwork();
	}

	protected void readData() throws Exception {
    	File file = new File(workingDir+inputFile);
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
							Integer index = vertexNameIndex.getIndexByName(yao);
							if(index==null) {
								yaoList.add(indexYao);
								vertexNameIndex.add(indexYao++, yao);
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
		vertexNameIndex.print(workingDir+fileYaoCode);
		
	}

	protected void buildNetwork() throws Exception {
		for(int i=0; i<fangYao.size(); i++) {
			HashSet<Integer> yaoList1 = fangYao.get(i);
			int countYao = yaoList1.size();
			Integer[] yaoArr = new Integer[countYao];
			yaoList1.toArray(yaoArr);
			for(int j=0; j<countYao; j++) {
				for(int k=j+1; k<countYao; k++) {
					network.addEdge(network.getEdgeCount()+1, yaoArr[j], yaoArr[k], EdgeType.UNDIRECTED);
				}
			}
		}
		refreshNetwork();
	}

	protected void buildPartialNetwork(double percentage, boolean random) {
		int countTotal = fangYao.size();
		int count = (int)(percentage * countTotal);
		
		if(random) {
			HashSet<Integer> usedSet = new HashSet<Integer>();
			while(usedSet.size()<count) {
				int index = (int)Math.round(Math.random() * (countTotal-1));
				if(!usedSet.contains(index)) {
					usedSet.add(index);
					addHyperNode(fangYao.get(index));
				}
			}
		} else {
			for(int i=0; i<count; i++) {
				addHyperNode(fangYao.get(i));
			}
		}
		
		refreshNetwork();
	}

	protected void buildHyperNetwork() throws IOException {
		for(int i=0; i<vertexNameIndex.size(); i++) {
			network.addVertex(i);
		}
		int progress = 0;
		for(HashSet<Integer> yaoList : fangYao.values()) {
//			if(progress++ % 10000 ==0) {
//				System.out.println(progress + ":\t" + new Date());
//				refreshNetwork();
//			}
			addHyperNode(yaoList);
		}
		
		refreshNetwork();
	}

	protected void buildHyperNetworkRandom(int count) throws IOException {
		if(count<fangYao.size()) {
//			for(int i=0; i<vertexNameIndex.size(); i++) {
//				network.addVertex(i);
//			}
			HashSet<Integer> usedSet = new HashSet<Integer>();
			while(usedSet.size()<count) {
				int index = (int)Math.round(Math.random() * (count-1));
				if(!usedSet.contains(index)) {
					usedSet.add(index);
					HashSet<Integer> yaoList = fangYao.get(index);
					for(Integer yao : yaoList) {
						if(!network.containsVertex(yao)) {
							network.addVertex(yao);
						}
					}
					addHyperNode(yaoList);
				}
			}
			refreshNetwork();
		} else {
			buildHyperNetwork();
		}
	}

	protected void addHyperNode(HashSet<Integer> yaoList) {
		int countYao = yaoList.size();
		Integer[] yaoArr = new Integer[countYao];
		yaoList.toArray(yaoArr);
		for(int j=0; j<countYao; j++) {
			for(int k=j+1; k<countYao; k++) {
				network.addEdge(network.getEdgeCount()+1, yaoArr[j], yaoArr[k], EdgeType.UNDIRECTED);
			}
		}
	}

	protected void reverseFangYao(String path) throws Exception {
		HashMap<Integer, HashSet<Integer>> yaoFang = new HashMap<Integer, HashSet<Integer>>();
		for(int indexFang : fangYao.keySet()) {
			HashSet<Integer> yaos = fangYao.get(indexFang);
			for(int indexYao : yaos) {
				HashSet<Integer> fangs = yaoFang.get(indexYao);
				if(fangs==null) {
					fangs = new HashSet<Integer>();
					yaoFang.put(indexYao, fangs);
				}
				fangs.add(indexFang);
			}
		}
				
		FileWriter out = new FileWriter(new File(workingDir+path));
		for(Integer indexYao : yaoFang.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(vertexNameIndex.getNameByIndex(indexYao)).append(": ");
			HashSet<Integer> fangs = yaoFang.get(indexYao);
			for(int indexFang : fangs) {
				sb.append(fangNameIndex.getNameByIndex(indexFang)).append(" ");
			}
			sb.append("\n");
			out.write(sb.toString());
			System.out.print(sb.toString());
		}
		out.close();
	}


	public static void main(String[] args) throws Exception 
	{
		System.out.println(new Date());

		HyperGraph graph = new HyperGraph();
		graph.workingDir = "C:/Users/sandy/Documents/MATLAB/javagraph/data/hypergraph/";
//		graph.inputFile = "FangYao.txt";
		graph.inputFile = "metabolites.txt";
//		graph.inputFile = "pubmedGene.txt";
		
		graph.readData();
//		graph.reverseFangYao("YaoFang.txt");
		graph.buildHyperNetwork();
		
		int vertexCount = graph.vertexList.size();
		
				
//		graph.printSummary = false;
//		graph.printDetail = false;
//		for(int i=0; i<vertexCount; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			graph.removeVertexFromHighestDegree(100);
//			graph.condenseNetworkByVertexComponentDegree(1);
//			System.out.print(i + "\t" + graph.vertexList.size());
//			graph.removeVertexByDegreeBelow(1);
//			System.out.println("\t" + graph.vertexList.size());
//		}
		
//		graph.removeVertexFromHighestDegree(50);
//		graph.focusOnGC();
//		graph.viewNetwork();
//		graph.condenseNetworkByVertexComponentDegree(1);
//		graph.viewNetwork();
		
//		graph.printDetail = true;
//		graph.setFileDetail("degreeGene.txt");
//		graph.getVertexDegrees();
//		graph.setFileDetail("componentDegreeGene.txt");
//		graph.getVertexComponentDegrees();
		
// test for component degree attribute 
		
		graph.removeVertexByDegree(1,true);
		graph.getVertexComponentDegrees();
		
// consider the degree 2 condition of the 	
//		HashMap<Number, Integer> Degree2Map = new HashMap<Number, Integer>();
//		Degree2Map=graph.getVertexDegrees(2);
  
		//		for(Number vetrtex : Degree2Map){
//			
//		}
//		int Degree2Map=graph.getVertexDegrees(2);
//		ArrayList<Number> removedVertex = new ArrayList<Number>(graph.vertexList);
//		Collections.sort(removedVertex, graph.degreeComparator);
//		Collections.reverse(removedVertex);
//		graph.printDetail = false;
//		graph.printSummary = false;
//		HashMap<Number, Double> componentDegreeMap = new HashMap<Number, Double>();
//		for(Number vertex : removedVertex) {
//			int index = vertex.intValue();
//			int maxDegree = 0;
//			graph.reset();
//			graph.buildHyperNetwork();
//			for(Number v : removedVertex) {
//				if(v.intValue()!=index) {
//					graph.network.removeVertex(v);
//					int componentDegree = graph.getVertexComponentDegree(vertex);
//					if(maxDegree<componentDegree) {
//						maxDegree = componentDegree;
//					}
//				}
//			}
//			componentDegreeMap.put(vertex, (double)maxDegree);
//			System.out.println(index + "\t" + maxDegree);
//		}
//		graph.mapHolder = componentDegreeMap;
//		Collections.sort(removedVertex, graph.mapValueComparator);
//		Collections.reverse(removedVertex);
//		System.out.println("removal count\tvertex id\tvertex count\tcomponent count total\tcomponent count average" +
//		"\tvertex count without degree1\tcomponent count total without degree1\tcomponent count average without degree1");
//		graph.reset();
//		graph.buildHyperNetwork();
//		for(int i=0; i<removedVertex.size(); i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			for(int j=0; j<i; j++) {
//				Number vertex = removedVertex.get(j);
//				graph.network.removeVertex(vertex);
//			}
//			graph.refreshNetwork();
//			System.out.print("\n" + i + "\t" + removedVertex.get(i));
//			HashMap<Number, Integer> degreeMap = graph.getVertexComponentDegrees();
//			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
//			graph.removeByVertexComponentDegreesLowest(1, degreeMap);
//			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
//		}


//		graph.getVertexDegrees();
//		graph.getNetworkVertexDegreeDistribution();
//		System.out.println("getNetworkVertexDegreeDistributionEntropy = " + graph.getNetworkVertexDegreeDistributionEntropy());
		
//		graph.focusOnGC();
//		graph.buildNetwork();
//		graph.buildPartialNetwork(0.1, false);
//		graph.getDegrees("yao_degree.txt");
//		graph.getLargestClique(true, "largest_clique.txt");
//		graph.getComponents(true);
//		graph.removeVertexByDegree(1000000);
//		graph.viewNetwork();
//		graph.removeEdgeByBetweenness(10000000);
		
//		for(int i=0; graph.vertexList.size()>0; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			graph.removeVertexFromHighestDegree(i);
//			System.out.println(i + "\t" + graph.vertexList.size() + "\t" + graph.getComponents(false).size());
//		}
		
//		graph.removeVertexByDegree(36);
//		graph.removeVertexByDegree1(10);
//		graph.removeEdgeByBetweenness(500, true);
//		graph.getComponents(true);
//		graph.viewNetwork();
		
//		graph.removeVertexByDegree(36);
//		graph.removeEdgeByHighDegree(3);
//		graph.getComponents(true);
////		graph.viewNetwork();
//		
//		graph.removeVertexByDegreeBelow(2);
//		graph.removeVertexFromHighestDegree(36);	
//		graph.getNeighborDegreeDistribution(1, 400);

//		graph.removeVertexByDegree(36);
//		for(int i=10; i<20; i++) {
//			for(int j=1; j<50; j++) {
//				graph.clusterByLowDegree(i, j);
//			}
//		}
//		graph.viewNetwork();
	
//		graph.removeVertexByDegree1(1);
//		graph.removeVertexByDegree(50);	
//		for(int i=1; i<40; i++) {
////			graph.removeVertexByDegree(1);
//			graph.clusterByNeighbor(i);
//		}
//		graph.clusterByNeighbor(6);
	
//		graph.removeVertexByDegreeBelow(20);
//		graph.removeVertexFromHighestDegree(100);
//		graph.focusOnGC();
//		graph.viewNetwork();
//		graph.getVertexDegrees("vertexDegree.txt", true);
//		graph.focusOnGC();
//		graph.getEdgeDegrees("edgeDegreeMetabolite.txt", false);
//		graph.getVertexEdgeDegrees("vertexEdgeDegreeFangji.txt", DISTRIBUTION_VALUE_TYPE_AVERAGE);
//		graph.getVertexEdgeDegrees("vertexEdgeDegreeMetabolite.txt", DISTRIBUTION_VALUE_TYPE_AVERAGE);

//		graph.getVertexEdgeDegrees("vertexEdgeDegree.txt", DISTRIBUTION_VALUE_TYPE_MAX);
		
//		while(graph.vertexList.size()>10) {
//		//	ng.removeVertexByDegreeBelow(i);
////			graph.removeVertexFromHighestDegree(3);
//			graph.removeVertexFromLowestDegree(15);
//			graph.focusOnGC();
//			System.out.println(graph.numberFormat.format(graph.getVertexEdgeDegrees("vertexEdgeDegree.txt", DISTRIBUTION_VALUE_TYPE_AVERAGE))
//					+ "\t" + graph.numberFormat.format(graph.getEdgeDegrees("edgeDegree.txt", true)));
//			
//		}

//		while(graph.vertexList.size()>0) {
//			graph.focusOnGC(false);
////			System.out.println(graph.vertexList.size() + "\t" + graph.numberFormat.format(graph.getVertexComponentDegreeValue("vertexComponentDegreeFangji.txt")));
//			System.out.print(graph.vertexList.size() + "\t");
//			graph.getVertexComponentDegreeValueWithout1("vertexComponentDegreeFangji.txt");
//			graph.removeVertexFromHighestDegree(1);
//		}
	
//		for(int i=0; i<100; i++) {
//			graph.focusOnGC(false);
//			graph.removeVertexFromHighestDegree(1);
//		}
//		graph.removeVertexFromHighestDegree(80);
//		graph.focusOnGC(false);
//		graph.viewNetwork();
//		graph.removeByVertexComponentDegreesLowest(1);
//		graph.viewNetwork();
//		ArrayList<Number> removedVertex = new ArrayList<Number>(graph.removeVertexFromHighestDegree(80));
//		graph.reset();
//		graph.buildHyperNetwork();
//		Collections.sort(removedVertex, graph.degreeComparator);
//		Collections.reverse(removedVertex);
//		graph.getVertexComponentDegreeValue("vertexComponentDegreeFangji.txt");
//		int count = 0;
//		for(Number vertex : removedVertex) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			HashSet<Number> reserveSet = new HashSet<Number>();
//			reserveSet.add(vertex);
//			graph.removeVertexByDegreeByCountWithReserve(0, 80, true, reserveSet);
//			graph.getVertexComponentDegreeValue("vertexComponentDegreeFangji_" + (count++) + "_" + vertex + ".txt");
//		}
		
//		for(Number vertex : removedVertex) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			HashSet<Number> reserveSet = new HashSet<Number>();
//			reserveSet.add(vertex);
//			int count = 0;
//			for(Number vertexRemove : removedVertex) {
//				if(!vertex.equals(vertexRemove)) {
//					graph.network.removeVertex(vertexRemove);
//					graph.focusOnGC(false);
//					System.out.println(vertex + "\t" + (count++) + "\t" + (graph.vertexList.size()) + "\t" + graph.getVertexComponentDegree(vertex));
//				}
//			}
////			graph.removeByVertexComponentDegreesLowest(1);
//			graph.viewNetwork();
//		}

//		graph.removeVertexFromHighestDegree(120);
//		graph.focusOnGC(true);
//		int vertexCount = 0;
//		for(int i=0; i<100; i++) {
//			System.out.println(i);
//			graph.removeByVertexComponentDegreesLowest(1);
//			if(vertexCount==graph.vertexList.size()) {
//				break;
//			} else {
//				vertexCount=graph.vertexList.size();
//			}
//		}
//		graph.removeVertexFromLowestDegree(5);
//		graph.viewNetwork();

		
//		graph.printSummary = false;
//		graph.printDetail = false;
//		graph.setFileDetail("tmp.txt");
//		System.out.println("removal count\tvertex count\tcomponent count total\tcomponent count average" +
//				"\tvertex count without degree1\tcomponent count total without degree1\tcomponent count average without degree1");
//		for(int i=1140; i<vertexCount; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			graph.removeVertexFromHighestDegree(i);
//			System.out.print("\n" + i);
//			HashMap<Number, Integer> degreeMap = graph.getVertexComponentDegrees();
//			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
//			graph.removeByVertexComponentDegreesLowest(1, degreeMap);
//			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
//		}
//		
//		for(int i=4500; i<vertexCount; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			graph.removeVertexFromHighestDegree(i);
//			int count = graph.vertexList.size();
//			System.out.print("\n" + i + "\t" + vertexCount);
//			for(int j=0; j<100; j++) {
//				graph.removeByVertexComponentDegreesLowest(1);
//				System.out.print("\t" + graph.vertexList.size());
//				if(count==graph.vertexList.size()) {
//					break;
//				} else {
//					count=graph.vertexList.size();
//				}
//			}
////			graph.viewNetwork();
//		}

//		graph.printSummary = false;
//		graph.setFileDetail("tmp.txt");
//		int vertexCount = graph.vertexList.size();
//		for(int i=0; i<vertexCount; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			graph.removeVertexFromHighestDegree(i);
//			graph.removeByVertexComponentDegreesLowest(1);
//			System.out.println(i + "\t" + graph.vertexList.size() + "\t" + new Date());
//		}
//		for(int i=0; graph.vertexList.size()>0; i++) {
//			graph.reset();
//			graph.buildHyperNetwork();
//			for(int j=0; j<i; j++) {
//				graph.removeVertexFromHighestDegree(1);
//				graph.focusOnGC(false);
//			}
//			graph.removeByVertexComponentDegreesLowest(1);
//			System.out.println(i + "\t" + graph.vertexList.size());
//		}

//		graph.reset();
//		graph.buildHyperNetwork();
//		int countHighest = 80;
//		graph.removeVertexFromHighestDegree(countHighest);
//		graph.refreshNetwork();
////		graph.focusOnGC(true);
////		graph.viewNetwork();
//		
//		graph.reset();
//		graph.buildHyperNetwork();
//		graph.removeVertexFromLowestDegree(graph.vertexList.size() - countHighest);
//		graph.refreshNetwork();
////		graph.viewNetwork();
//		
		
		System.out.println(new Date());
	}

}
