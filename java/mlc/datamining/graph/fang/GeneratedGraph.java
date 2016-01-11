package mlc.datamining.graph.fang;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.algorithms.util.DiscreteDistribution;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author chen_lu
 *
 */
public class GeneratedGraph extends BaseGraph {

	public static final int TYPE_POWERLAW_BARABASIALBERT = 1; 
	public static final int TYPE_POWERLAW_EPPSTEIN = 2; 
	public static final int TYPE_RANDOM_ERDOSRENYI = 3; 
	public static final int TYPE_RANDOM_KLEINBERGSMALLWORLD = 4; 
	public static final int TYPE_RANDOM_MIXED = 5; 

	int graphType;
	int numOfVertex;
	int numOfEdge;
	int numOfIteration = 1000000; 
	double connectionProbability;
	
	GraphGenerator<Number, Number> generator;
	
	static class GraphFactory implements Factory<Graph<Number, Number>> {
		public UndirectedSparseGraph<Number, Number> create() {
			return new UndirectedSparseGraph<Number, Number>();
		}
	}
	
	static class UndirectedGraphFactory implements Factory<UndirectedGraph<Number, Number>> {
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
		System.out.println("Warning: no network is created with this constructor.");
	}

	public GeneratedGraph(int type, int numOfV, int numOfE, int numOfIter) {
		this.graphType = type;
		this.numOfVertex = numOfV;
		this.numOfEdge = numOfE;
		this.numOfIteration = numOfIter;
		switch(this.graphType) {
			case TYPE_POWERLAW_EPPSTEIN:
				this.generator = new EppsteinPowerLawGenerator<Number, Number>(
						new GraphFactory(), new NumberFactory(), new NumberFactory(), numOfVertex, numOfEdge, numOfIteration);
				break;
			case TYPE_POWERLAW_BARABASIALBERT:
				this.generator = new BarabasiAlbertGenerator<Number, Number>(
						new GraphFactory(), new NumberFactory(), new NumberFactory(), numOfVertex, numOfEdge, new HashSet<Number>());
				break;
			default: 
				System.out.println("Error: no graph type selected or wrong graph type.");
		}
		rebuild();
	}
	
	public GeneratedGraph(int type, int numOfV, double probility) {
		this.graphType = type;
		this.numOfVertex = numOfV;
		switch(this.graphType) {
			case TYPE_RANDOM_KLEINBERGSMALLWORLD:
				System.out.println("Error: not implemented yet!");
				break;
			case TYPE_RANDOM_MIXED:
				System.out.println("Error: not implemented yet!");
				break;
			case TYPE_RANDOM_ERDOSRENYI:
				this.connectionProbability = probility;
				this.generator = new ErdosRenyiGenerator<Number, Number>(new UndirectedGraphFactory(), 
						new NumberFactory(), new NumberFactory(), numOfVertex, connectionProbability);
				break;
			default: 
				System.out.println("Error: no graph type selected or wrong graph type.");
		}
		rebuild();
	}
	
	public void rebuild() {
		if(this.graphType==TYPE_POWERLAW_BARABASIALBERT) {
			((BarabasiAlbertGenerator)generator).evolveGraph(numOfIteration);
		}
		network = (UndirectedSparseGraph<Number, Number>) generator.create();
		
		//remove circle edges
		Set<Number> edges = new HashSet<Number>();
		edges.addAll(network.getEdges());
		for(Number edge : edges) {
			Pair<Number> endpoints = network.getEndpoints(edge);
            if(endpoints.getFirst().equals(endpoints.getSecond())) {
            	network.removeEdge(edge);
            }
        }
		refreshNetwork();
	}


	
	public static void main(String[] args) throws Exception 
	{
		GeneratedGraph graph = new GeneratedGraph(3000, 9000, 1000000);
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
