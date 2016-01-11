/**
 * Copyright Â© 2000-2008 MedPlus, Inc. All rights reserved.
 */
package mlc.datamining.graph.fang;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author chen_lu
 *
 */
public class GraphTest {
	public static void main(String[] args) throws Exception 
	{
		System.out.println(new Date());

		//size=383, edge=3944, 5.39%
//		BaseGraph graph = new HyperGraph("C:/data/dev/java/tcm/data/text/hypergraph/", "FangYao.txt");
		//size=813, edge=4784, 1.45%
//		BaseGraph graph = new HyperGraph("C:/data/dev/java/tcm/data/text/hypergraph/", "metabolites.txt");
		//size=16,824
//		BaseGraph graph = new HyperGraph("C:/data/dev/java/tcm/data/text/hypergraph/", "pubmedGene.txt");
		
		//size=1,589, edge=2742, 0.22%
//		BaseGraph graph = new NewmanGraph("C:/data/dev/java/tcm/data/text/newman/", "netscience.gml");
		//size=62
//		BaseGraph graph = new NewmanGraph("C:/data/dev/java/tcm/data/text/newman/", "dolphins.gml");
		//size=16,706
//		BaseGraph graph = new NewmanGraph("C:/data/dev/java/tcm/data/text/newman/", "astro-ph.gml");
		//size=22,963
//		BaseGraph graph = new NewmanGraph("C:/data/dev/java/tcm/data/text/newman/", "as-22july06.gml");
		
		//size=1870
//		BaseGraph graph = new PlainHyperGraph("C:/data/dev/java/tcm/data/text/plaingraph/", "bo.dat");
		//size=1057
//		BaseGraph graph = new PlainHyperGraph("C:/data/dev/java/tcm/data/text/plaingraph/", "cell_metabolite_AA.dat");
		//size=383,640
//		BaseGraph graph = new PlainHyperGraph("C:/data/dev/java/tcm/data/text/plaingraph/", "actor.dat");
		//size=325,729
//		BaseGraph graph = new PlainHyperGraph("C:/data/dev/java/tcm/data/text/plaingraph/", "www.dat");
		
//		BaseGraph graph = new GeneratedGraph(383, 3944, 10000000);
//		BaseGraph graph = new GeneratedGraph(813, 4784, 10000000);

	int countV = 10;
		int countE = 20;
	BaseGraph graph = new GeneratedGraph(countV, countE, 1000000);
//		System.out.println("removal count\tvertex count\tcomponent count total\tcomponent count average" +
//		"\tvertex count without degree1\tcomponent count total without degree1\tcomponent count average without degree1");
//		for(int i=0; i<countV && i<1000; i++) {
//			BaseGraph graph2 = new GeneratedGraph();
//			graph2.printSummary = false;
//			graph2.printDetail = false;
//			graph2.network = graph.cloneNetwok();
//			graph2.refreshNetwork();
//			graph2.removeVertexFromHighestDegree(i);
//			System.out.print("\n" + i);
//			HashMap<Number, Integer> degreeMap = graph2.getVertexComponentDegrees();
//			System.out.print("\t" + graph2.vertexList.size() + "\t" + graph2.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + BaseGraph.formatDecimal.format(graph2.getNetworkVertexComponentDegreeAverage(degreeMap)));
//			graph2.removeByVertexComponentDegreesLowest(1, degreeMap);
//			System.out.print("\t" + graph2.vertexList.size() + "\t" + graph2.getNetworkVertexComponentDegreeTotal(degreeMap) 
//					+ "\t" + BaseGraph.formatDecimal.format(graph2.getNetworkVertexComponentDegreeAverage(degreeMap)));
//		}
//		
//		BaseGraph graph = new GeneratedGraph();
		int interation = 3;
		System.out.println("numOfV\tnumOfE\tedge density\thighestV degree\thighestV component degree\thighestV normalized componentdegree"); 
		for(int numOfV = 30000; numOfV<40000; numOfV += numOfV) {
			int maxNumOfE = numOfV * numOfV / 16;
			for(int numOfE=numOfV; numOfE<maxNumOfE; numOfE+=numOfE) {
				double totalDegree = 0;
				double totalComponentDegree = 0;
				double totalNormalizedComponentDegree = 0.0;
				for(int i=0; i<interation; i++) {
					graph.printSummary = false;
					graph.printDetail = false;
					((GeneratedGraph)graph).generateNetwork(numOfV, numOfE, 1000000);
					ArrayList<Number> vertexList = new ArrayList<Number>(graph.vertexList);
					Collections.sort(vertexList, graph.degreeComparator);
					Collections.reverse(vertexList);
					Number vertex = vertexList.get(0);
					HashMap<Number, Integer> componentDegreeMap = graph.getVertexComponentDegrees();
					double degree = graph.network.getNeighborCount(vertex);
					totalDegree += degree;
					double componentDegree = componentDegreeMap.get(vertex);
					totalComponentDegree += componentDegree;
					totalNormalizedComponentDegree += componentDegree / degree;
					
//					System.out.println("(" + n + ", " + m + ")\t" + 
//							i + "\t" + vertex + "\t" + graph.network.getNeighborCount(vertex) + "\t" + componentDegreeMap.get(vertex));
				}
				System.out.println(numOfV + "\t" + numOfE + "\t" 
						+ BaseGraph.formatPercentage.format(((double)numOfE)/(numOfV*numOfV)) + "\t"
						+ BaseGraph.formatDecimal.format(totalDegree/interation) + "\t"
						+ BaseGraph.formatDecimal.format(totalComponentDegree/interation) + "\t" 
						+ BaseGraph.formatScientific.format(totalNormalizedComponentDegree/interation));

			}
		}

		
		graph.printSummary = false;
		graph.printDetail = false;
		int vertexCount = graph.vertexList.size();
		
 		for(int i=0; i<vertexCount; i++) {
//			graph.removeVertexFromHighestDegree(1);
			System.out.println("vertex id\tdegree\tcomponent degree");
			HashMap<Number, Integer> componentDegreeMap = graph.getVertexComponentDegrees();
			
			ArrayList<Number> vertexList = new ArrayList<Number>(graph.vertexList);
			Collections.sort(vertexList, graph.degreeComparator);
			Collections.reverse(vertexList);
			for(Number vertex : vertexList) {
				System.out.println(vertex + "\t" + graph.network.getNeighborCount(vertex) + "\t" + componentDegreeMap.get(vertex));
			}
		}
		
//		graph.viewNetwork();

//		System.out.println("0\t" + graph.vertexList.size() + "\t" + graph.getNetworkComponents().size());
////		graph.viewNetwork();
		graph.removeVertexFromHighestDegree(10); //69 for fangji, 18 for metabolite, 39 for netscience 
		for(int i=1; ;i++) {
			graph.removeByVertexComponentDegreesHighest(2);
//			graph.removeByVertexComponentDegreesLowest(0);
			System.out.println(i + "\t" + graph.vertexList.size() + "\t" + graph.getNetworkComponents().size());
//			graph.viewNetwork();
			if(vertexCount>graph.vertexList.size()) {
				vertexCount = graph.vertexList.size();
			} else {
				break;
			}
		}
		System.out.println(graph.getNetworkComponents());
////		graph.viewNetwork();
////		System.out.println(graph.getNetworkComponents());
//		vertexCount = graph.vertexList.size();
//		System.out.println("remove count\tnetwork size\tcomponent count\tGC size");
//		for(int i=0; i<vertexCount; i++) {
//			if(i>0) {
//				graph.rebuild();
//			}
//			if(i%10==0) {
//				System.out.println();
//			}
//			graph.removeVertexFromHighestDegree(i);
//			HashMap<Number, Integer> degreeMap = graph.getVertexComponentDegrees();
//			graph.removeByVertexComponentDegreesHighest(2, degreeMap);
//			graph.removeByVertexComponentDegreesLowest(0, degreeMap);
//			System.out.print(i + "\t" + graph.vertexList.size() + "\t" + graph.getNetworkComponents().size());
////			System.out.println(graph.getNetworkComponents());
////			graph.viewNetwork();
//			graph.focusOnGC();
//			System.out.print("\t" + graph.vertexList.size() + "\t,\t");
////			System.out.print(i + "\t" + graph.vertexList.size() + "\t" + graph.getNetworkComponents().size() +", ");
////			graph.viewNetwork();
//		}

		System.out.println("remove count\tnetwork size\tnetwork size after condense" +
				"\tnetwork size without degree1\tdegree1 component count");
 		for(int i=0; i<vertexCount; i++) {
			if(i>0) {
				graph.rebuild();
			}
			graph.removeVertexFromHighestDegree(i);
			HashMap<Number, HashSet<Number>> componentMap = graph.condenseNetworkByVertexComponentDegree(1);
//			for(HashSet<Number> componentSet : componentMap.values()) {
//				System.out.println(componentSet);
//			}
			System.out.print(i + "\t" + (vertexCount - i) + "\t" + graph.vertexList.size());
			graph.removeVertexByDegreeBelow(1);
			System.out.println("\t" + graph.vertexList.size() + "\t" + componentMap.size());
//			graph.viewNetwork();
		}
		

//		System.out.println("remove count\tnetwork size after condense\tnetwork size without degree1");
// 		for(int i=0; i<vertexCount; i++) {
//			if(i>0) {
//				graph.rebuild();
//			}
//			graph.removeVertexFromHighestDegree(i);
//			graph.condenseNetworkByVertexComponentDegree(1);
//			System.out.print(i + "\t" + graph.vertexList.size());
//			graph.removeVertexByDegreeBelow(1);
//			System.out.println("\t" + graph.vertexList.size());
////			graph.viewNetwork();
//		}
		
//		graph.removeVertexFromHighestDegree(50);
//		graph.focusOnGC();
//		graph.viewNetwork();
//		graph.condenseNetworkByVertexComponentDegree(1);
//		graph.viewNetwork();
		
		graph.printDetail = true;
//		graph.setFileDetail("degreeGene.txt");
//		graph.getVertexDegrees();
//		graph.setFileDetail("componentDegreeGene.txt");
//		graph.getVertexComponentDegrees();
		
		ArrayList<Number> removedVertex = new ArrayList<Number>(graph.vertexList);
		Collections.sort(removedVertex, graph.degreeComparator);
		Collections.reverse(removedVertex);
		graph.printDetail = false;
		graph.printSummary = false;
		HashMap<Number, Double> componentDegreeMap = new HashMap<Number, Double>();
		for(Number vertex : removedVertex) {
			int index = vertex.intValue();
			int maxDegree = 0;
			graph.rebuild();
			for(Number v : removedVertex) {
				if(v.intValue()!=index) {
					graph.network.removeVertex(v);
					int componentDegree = graph.getVertexComponentDegree(vertex);
					if(maxDegree<componentDegree) {
						maxDegree = componentDegree;
					}
				}
			}
			componentDegreeMap.put(vertex, (double)maxDegree);
			System.out.println(index + "\t" + maxDegree);
		}
		graph.mapHolder = componentDegreeMap;
		Collections.sort(removedVertex, graph.mapValueComparator);
		Collections.reverse(removedVertex);
		System.out.println("removal count\tvertex id\tvertex count\tcomponent count total\tcomponent count average" +
		"\tvertex count without degree1\tcomponent count total without degree1\tcomponent count average without degree1");
		graph.rebuild();
		for(int i=0; i<removedVertex.size(); i++) {
			graph.rebuild();
			for(int j=0; j<i; j++) {
				Number vertex = removedVertex.get(j);
				graph.network.removeVertex(vertex);
			}
			graph.refreshNetwork();
			System.out.print("\n" + i + "\t" + removedVertex.get(i));
			HashMap<Number, Integer> degreeMap = graph.getVertexComponentDegrees();
			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
					+ "\t" + graph.formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
			graph.removeByVertexComponentDegreesLowest(1, degreeMap);
			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
					+ "\t" + graph.formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
		}


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

		
		graph.printSummary = false;
		graph.printDetail = false;
		graph.setFileDetail("tmp.txt");
		System.out.println("removal count\tvertex count\tcomponent count total\tcomponent count average" +
				"\tvertex count without degree1\tcomponent count total without degree1\tcomponent count average without degree1");
		for(int i=1140; i<vertexCount; i++) {
			graph.rebuild();
			graph.removeVertexFromHighestDegree(i);
			System.out.print("\n" + i);
			HashMap<Number, Integer> degreeMap = graph.getVertexComponentDegrees();
			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
					+ "\t" + graph.formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
			graph.removeByVertexComponentDegreesLowest(1, degreeMap);
			System.out.print("\t" + graph.vertexList.size() + "\t" + graph.getNetworkVertexComponentDegreeTotal(degreeMap) 
					+ "\t" + graph.formatDecimal.format(graph.getNetworkVertexComponentDegreeAverage(degreeMap)));
		}
		
		for(int i=4500; i<vertexCount; i++) {
			graph.rebuild();
			graph.removeVertexFromHighestDegree(i);
			int count = graph.vertexList.size();
			System.out.print("\n" + i + "\t" + vertexCount);
			for(int j=0; j<100; j++) {
				graph.removeByVertexComponentDegreesLowest(1);
				System.out.print("\t" + graph.vertexList.size());
				if(count==graph.vertexList.size()) {
					break;
				} else {
					count=graph.vertexList.size();
				}
			}
//			graph.viewNetwork();
		}

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

		graph.rebuild();
		int countHighest = 80;
		graph.removeVertexFromHighestDegree(countHighest);
		graph.refreshNetwork();
//		graph.focusOnGC(true);
//		graph.viewNetwork();
		
		graph.rebuild();
		graph.removeVertexFromLowestDegree(graph.vertexList.size() - countHighest);
		graph.refreshNetwork();
//		graph.viewNetwork();
		
		
		System.out.println(new Date());
	}


}
