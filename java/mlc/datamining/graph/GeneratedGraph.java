/**
 * Copyright Â© 2000-2008 MedPlus, Inc. All rights reserved.
 */
package mlc.datamining.graph.fang;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.util.DiscreteDistribution;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author chen_lu
 *
 */
public class GeneratedGraph extends BaseGraph {

	int numOfVertex;
	int numOfEdge;
	int numOfIteration; 
	
	static class GraphFactory implements Factory<Graph<Number, Number>> {
		public UndirectedSparseGraph<Number, Number> create() {
			return new UndirectedSparseGraph<Number, Number>();
		}
	}
	
	static class NumberFactory implements Factory<Number> {
		int count;
		public Number create() {
			return count++;
		}
		
	}

	public GeneratedGraph() {
		super();
	}

	public GeneratedGraph(int numOfV, int numOfE, int numOfIteration) {
		super();
		generateNetwork(numOfV, numOfE, numOfIteration);
	}

	public void rebuild() throws Exception {
		network = new UndirectedSparseGraph();
		generatorEppsteinPowerLawGraph();
	}

	public void generateNetwork(int numOfV, int numOfE, int numOfIteration) {
		generatorEppsteinPowerLawGraph(numOfV, numOfE, numOfIteration);
	}
	
	public void generatorEppsteinPowerLawGraph(int numOfV, int numOfE, int numOfIter) {
		this.numOfVertex = numOfV;
		this.numOfEdge = numOfE;
		this.numOfIteration = numOfIter;
		generatorEppsteinPowerLawGraph();
		refreshNetwork();
	}
	
	public void generatorEppsteinPowerLawGraph() {
		EppsteinPowerLawGenerator generator = new EppsteinPowerLawGenerator<Number, Number>(
					new GraphFactory(), new NumberFactory(), new NumberFactory(), numOfVertex, numOfEdge, numOfIteration);
		network = (UndirectedSparseGraph<Number, Number>) generator.create();
		Set<Number> edges = new HashSet<Number>();
		edges.addAll(network.getEdges());
		for(Number edge : edges) {
			Pair<Number> endpoints = network.getEndpoints(edge);
            if(endpoints.getFirst().equals(endpoints.getSecond())) {
            	network.removeEdge(edge);
            }
        }
	}
	
	public static void main(String[] args) throws Exception 
	{
		GeneratedGraph graph = new GeneratedGraph();
		
		graph.generatorEppsteinPowerLawGraph(3000, 9000, 1000000);
		graph.focusOnGC();
		graph.refreshNetwork();
//		graph.workingDir = "C:/data/dev/java/tcm/data/text/hypergraph/";
//		System.out.println("network degree correlation value:\t " + formatDecimal.format(graph.getNetworkDegreeCorrelation()));
//		graph.setFileDetail("eppsteinVertexComponentDegree.txt");
//		System.out.println("network component degree value:\t " + formatDecimal.format(graph.getNetworkVertexComponentDegree()));
//		graph.setFileDetail("eppsteinVertexDegree.txt");
//		graph.getVertexDegrees();
//		graph.setFileDetail("eppsteinVertexDegreeDistribution.txt");
		graph.setFileDetail("");
		graph.getNetworkVertexDegreeDistribution();
		System.out.println("getNetworkVertexDegreeDistributionEntropy = " + graph.getNetworkVertexDegreeDistributionEntropy());
		
//		graph.setFileDetail("eppsteinVertexClusterCoef.txt");
//		graph.getVertexClusteringCoefficient();
		System.out.println("network clustering coefficient value:\t " + formatDecimal.format(graph.getNetworkClusteringCoefficient()));

		System.out.println(DiscreteDistribution.entropy(new double[] {1, 1, 1, 1}));

//		graph.viewNetwork();

	}
}
