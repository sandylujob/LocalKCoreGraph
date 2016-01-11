package mlc.datamining.graph.fang;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFrame;

import jsc.correlation.LinearCorrelation;
import jsc.curvefitting.ExponentialFit;
import jsc.curvefitting.LineFit;
import jsc.datastructures.PairedData;
import jsc.descriptive.FrequencyTable;
import jsc.util.Scale;
import mlc.datamining.cluster.Hierarchical;
import mlc.datamining.util.NameIndexPair;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.apache.commons.lang.StringUtils;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.DiscreteDistribution;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public abstract class BaseGraph {

	public static final double VERY_SMALL_NUMBER = 0.0000000001;
	
	public static final int DISTRIBUTION_VALUE_TYPE_AVERAGE = 0;
	public static final int DISTRIBUTION_VALUE_TYPE_MODE = 1;
	public static final int DISTRIBUTION_VALUE_TYPE_MIN = 2;
	public static final int DISTRIBUTION_VALUE_TYPE_MAX = 3;

	public static final DecimalFormat formatInteger = new DecimalFormat("#,###");
	public static final DecimalFormat formatDecimal = new DecimalFormat("0.##");
	public static final DecimalFormat formatPercentage = new DecimalFormat("0.##%");
	public static final DecimalFormat formatScientific = new DecimalFormat("0.###E0");
	
	public boolean printSummary = true;
	public boolean printDetail = false;
	public String fileSummary = "";
	public String fileDetail = "";
	
	NameIndexPair vertexNameIndex = new NameIndexPair();
	protected UndirectedSparseGraph<Number, Number> network = new UndirectedSparseGraph();
	protected ArrayList<Number> vertexList; 
	protected ArrayList<Number> edgeList; 
	protected VisualizationViewer viewer = null;

	protected String workingDir = "";
	protected String inputFile = "";

	protected Comparator numberComparator = new Comparator<Number>() {
	    public int compare(Number o1, Number o2) {
	    	return o1.intValue() - o2.intValue();
	    }};
	
    protected Comparator degreeComparator = new Comparator<Number>() {
	    public int compare(Number o1, Number o2) {
	    	return network.getNeighbors(o1).size() - network.getNeighbors(o2).size();
	    }};

	protected HashMap<Number, Double> mapHolder = null;
    protected Comparator mapValueComparator = new Comparator<Number>() {
	    public int compare(Number o1, Number o2) {
	    	return mapHolder.get(o1)<mapHolder.get(o2) ? -1 : 1;
	    }};

    protected  Transformer vertexLableTransformer = new Transformer<String,String>() {
		public String transform(String input) {
			return vertexNameIndex.getNameByIndex(input);
		}};

    protected class VertexTransformer<V, E> 
	    extends AbstractVertexShapeTransformer <V> implements Transformer<V,Shape>  {

    	DegreeScorer score = null;
    	int size = 15;
    	
    	public VertexTransformer(int sizeIn){
    		this.size = sizeIn;
            setSizeTransformer(new Transformer<V,Integer>() {
				public Integer transform(V v) {
					return size;
				}});
            
            setAspectRatioTransformer(new Transformer<V,Float>() {
				public Float transform(V v) {
		        	return 1.0f;
				}});
    	}

    	public VertexTransformer(DegreeScorer scoreIn){
            this.score = scoreIn;
            setSizeTransformer(new Transformer<V,Integer>() {
				public Integer transform(V v) {
					return score.getVertexScore(v) + 10;
				}});
            
            setAspectRatioTransformer(new Transformer<V,Float>() {
				public Float transform(V v) {
		        	return 1.0f;
				}});
    	}

		public Shape transform(V v) {
			return factory.getEllipse(v);
		}
    }

	public BaseGraph() {
		super();
	}
		
	public BaseGraph(String dir, String file) {
		this.workingDir = dir;
		this.inputFile = file;
	}
	
	public UndirectedSparseGraph<Number, Number> cloneNetwok() {
		UndirectedSparseGraph<Number, Number> newNetwork = new UndirectedSparseGraph();
		for(Number vertex : network.getVertices()) {
			newNetwork.addVertex(vertex);
		}

		for(Number edge : network.getEdges()) {
			Pair<Number> endpoints = network.getEndpoints(edge);
			newNetwork.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
		}
		
		return newNetwork;
	}
	
	/********************************************************************************
     * Utils
     ********************************************************************************/
	protected Writer getWriter(String outFile) throws IOException {
		if(StringUtils.isEmpty(outFile)) {
			return new PrintWriter(System.out);
		} else {
			return new FileWriter(new File(workingDir+outFile));
		}
	}

	public void print(String str) {
		print(fileDetail, str);
	}


	public void print(String file, String str) {
		try {
			Writer out = getWriter(file);
			out.write(str);
			closeWriter(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeWriter(Writer out) throws IOException  {
		if(out instanceof PrintWriter) {
			out.flush();
		} else {
			out.close();
		}
	}
    public void setFileDetail(String file) {
    	fileDetail = file;
    }
    
    public void setPrintDetail(boolean print) {
    	printDetail = print;
    }
    
    public void printOn() {
    	setPrintDetail(true);
    }
    
    public void printOff() {
    	setPrintDetail(false);
    }
    
    
    /********************************************************************************
     * Network operations
     ********************************************************************************/
    public abstract void rebuild() throws Exception;
    
	protected void refreshNetwork() {
		vertexList = new ArrayList<Number>(network.getVertices());
		Collections.sort(vertexList, numberComparator);
		
		edgeList = new ArrayList<Number>(network.getEdges());
		Collections.sort(edgeList,numberComparator);
		
		if(printSummary) {
			int edgeCount = edgeList.size();
			int vertexCount = vertexList.size();
			String str = "vertex count: " + vertexCount 
					+ "\nedge count: " + edgeCount + "/" + (vertexCount*(vertexCount-1)/2) 
					+ " = " + formatPercentage.format(edgeCount*2.0/(vertexCount*(vertexCount-1)))
					+ "\n";
			print(fileSummary, str);
		}
	}
	
	public void viewNetwork() {
//		viewNetwork(new FRLayout2(network));
		//ok for spread network, quick. try multiple time to get a good one
//		viewNetwork(new KKLayout(network));
		//good for separate components, and very quick.
		viewNetwork(new ISOMLayout(network));
		//this is the best viewer, but very slow to be stable
//		viewNetwork(new SpringLayout(network));
	}
    
	public void viewNetwork(Layout layout) {
		Dimension size = new Dimension(800,800);
	    viewer = new VisualizationViewer(layout, size);
	    viewer.setBackground(Color.white);
//	    DegreeScorer degreeScore = new DegreeScorer(network);
//	    viewer.getRenderContext().setVertexShapeTransformer(new VertexTransformer(degreeScore)); 
//	    viewer.getRenderContext().setVertexShapeTransformer(new VertexTransformer(10)); 
	    viewer.getRenderContext().setVertexFillPaintTransformer(
	    	new PickableVertexPaintTransformer<String>(viewer.getPickedVertexState(), Color.lightGray, Color.gray));
	    viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
	    viewer.getRenderContext().setVertexLabelTransformer(
			new ChainedTransformer<String,String>(new Transformer[]{new ToStringLabeller<String>(),	vertexLableTransformer}));
	    viewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	    DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
	    gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
	    viewer.setGraphMouse(gm);
	    viewer.addKeyListener(gm.getModeKeyListener());
	    
	    JFrame jf = new JFrame();
	    jf.getContentPane().add(viewer);
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jf.pack();
	    jf.setVisible(true);
	}
	

    /********************************************************************************
     * Network as whole properties
     ********************************************************************************/
	public double getNetworkDegreeCorrelation() {
		double[][] degreePair = new double[network.getEdgeCount()][2];
		int index = 0;
		for(Number edge : edgeList) {
			Pair<Number> pair = network.getEndpoints(edge);
			Number end1 = pair.getFirst();
			Number end2 = pair.getSecond();
			degreePair[index][0] = network.getNeighborCount(end1);
			degreePair[index][1] = network.getNeighborCount(end2);
			index++;
		}
		
		double correlation = LinearCorrelation.correlationCoeff(new PairedData(degreePair));
		return correlation;
	}

	public void getNetworkClosenessDistribution(int numOfBucket) {
		UnweightedShortestPath shortestPath = new UnweightedShortestPath(network);
		int size = vertexList.size();
		double[] closenesses = new double[size];
		double min = vertexList.size();
		double max = 0;
		
		int index = 0;
		for(Number vertex : vertexList) {
			Map<Number, Number> distanceMap = shortestPath.getDistanceMap(vertex);
			double average = 0;
			int count = 0;
			for(Number vertex2 : vertexList) {
				Number distance = distanceMap.get(vertex2);
				if(distance!=null) {
					count++;
					average += (Integer)(distanceMap.get(vertex2)).intValue();
				}
			}
			average /= count;
			closenesses[index] = average;
//			if(min > average) {
//				min = average;
//			}
			if(max < average) {
				max = average;
			}
			index++;
		}
		min = 0.0001;
		int[] counts = new int[numOfBucket];
		double interval = (max+0.00001 - min)/numOfBucket;
		for(int i=0; i<size; i++) {
			int range = (int)Math.floor((closenesses[i]-min) / interval );
			counts[range]++;
		}
		
		for(int i=0; i<numOfBucket; i++) {
			double low = min + interval*i;
			System.out.println(i + "\t" + counts[i] + "\t" 
					+ formatDecimal.format(low) + "-" + formatDecimal.format(low+interval));
		}
	}

	public double getNetworkShortestPathDistribution() {
		return getNetworkShortestPathDistribution(0);
	}

	public double getNetworkShortestPathDistribution(int minDegree) {
		UnweightedShortestPath shortestPath = new UnweightedShortestPath(network);
		int min = vertexList.size();
		int max = 0;
		
		for(Number vertex : vertexList) {
			Map<Number, Number> distanceMap = shortestPath.getDistanceMap(vertex);
			for(Number vertex2 : vertexList) {
				Number distance = distanceMap.get(vertex2);
				if(distance!=null) {
					int distInt = (Integer)(distance).intValue();
					if(min>distInt) {
						min = distInt;
					}
					if(max<distInt) {
						max = distInt;
					}
				}
			}
		}
		
		int[] counts = new int[max+1];
		for(Number vertex : vertexList) {
			Map<Number, Number> distanceMap = shortestPath.getDistanceMap(vertex);
			for(Number vertex2 : vertexList) {
				if(network.getNeighborCount(vertex2)>minDegree) {
					Number distance = distanceMap.get(vertex2);
					if(distance!=null) {
						int distInt = (Integer)(distance).intValue();
						counts[distInt]++;
					}
					
				}
			}
		}
		
		for(int i=0; i<=max; i++) {
			System.out.println(i + "\t" + counts[i]);
		}
		
		return counts[3]*1.3 - counts[2];
	}

	public Set<Set<Number>> getNetworkComponents() {
		WeakComponentClusterer cluster = new WeakComponentClusterer();
		Set<Set<Number>> componentSet = cluster.transform(network);
		
		if(printDetail) {
			StringBuilder sb = new StringBuilder();
			sb.append("component count:\t" + componentSet.size() + "\n");
			for(Set<Number> set : componentSet) {
				sb.append(set.toString() + "\n");
			}
			print(sb.toString());
		}
		return componentSet;
	}

	public HashMap<Integer, Integer> getNetworkVertexDegreeDistribution(int frequencySize) {
		HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();
		for(Number vertex : vertexList) {
			int degreeNeighbor = network.getNeighborCount(vertex) / frequencySize;
			Integer count = distribution.get(degreeNeighbor);
			if(count!=null) {
				distribution.put(degreeNeighbor, count.intValue()+1);
			} else {
				distribution.put(degreeNeighbor, 1);
			}
		}
		return distribution;
	}
	
	public int getNetworkVertexComponentDegreeTotal() {
		return getNetworkVertexComponentDegreeTotal(getVertexComponentDegrees());
	}

	public int getNetworkVertexComponentDegreeTotal(HashMap<Number, Integer> degreeMap) {
		int total = 0;
		for(Number vertex : vertexList) {
			total += degreeMap.get(vertex);
		}
		return total;
	}

	public double getNetworkVertexComponentDegreeAverage() {
		return getNetworkVertexComponentDegreeAverage(getVertexComponentDegrees());
	}
	
	public double getNetworkVertexComponentDegreeAverage(HashMap<Number, Integer> degreeMap) {
		double total = 0;
		for(Number vertex : vertexList) {
			total += degreeMap.get(vertex);
		}
		return total/vertexList.size();
	}
	
	public double getNetworkClusteringCoefficient() {
		Map<Number,Double> map = Metrics.clusteringCoefficients(network);
		double total = 0;
		for(Number vertex : map.keySet()) {
			total += map.get(vertex);
		}
		return total / map.size();
	}

	public void getNetworkVertexDegreeDistribution() {
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		
//		double[] degrees = new double[vertexArr.size()];
//		int index = 0;
//		int max = 0;
//		for(Number vertex : vertexArr) {
//			degrees[index] = network.getNeighborCount(vertex);
//			if(max<degrees[index]) {
//				max = (int) degrees[index];
//			}
//			index++;
//		}
//		
//		FrequencyTable table = new FrequencyTable("vertexDegree", 0, max, 1, degrees);
//		StringBuilder sb = new StringBuilder();
//		double[] frequency = new double[max-1];
//		double[] x = new double[max-1];
//		double[] y = new double[max-1];
//		for(int i=1; i<max; i++) {
//			x[i-1] = Math.log(i);
//			frequency[i-1] = table.getFrequency(i);
//			sb.append(i).append("\t").append(frequency[i-1]).append("\n");
//			if(frequency[i-1]>0) {
//				y[i-1] = Math.log(frequency[i-1]);
//			}
////			y[i-1] = Math.log(i*i);
//		}
		
		double[] degrees = new double[vertexArr.size()];
		int index = 0;
		int max = 0;
		for(Number vertex : vertexArr) {
			degrees[index] = network.getNeighborCount(vertex);
			if(max<degrees[index]) {
				max = (int) degrees[index];
			}
			index++;
		}
		
		FrequencyTable table = new FrequencyTable("vertexDegree", 0, max, 1, degrees);
		StringBuilder sb = new StringBuilder();
//		double[] frequency = new double[max-1];
		HashMap<Integer, Integer> xy = new HashMap<Integer, Integer>();
		for(int i=2; i<max; i++) {
			int frequency = table.getFrequency(i);
			if(frequency>1) {
				xy.put(i, frequency);
			}
		}
		int count = xy.size();
		double[] x = new double[count];
		double[] y = new double[count];
		int i = 0;
		for(int key : xy.keySet()) {
			x[i] = Math.log(key);
			y[i] = Math.log(xy.get(key));
			sb.append(key).append("\t").append(xy.get(key)).append("\n");
			i++;
		}
		
		PairedData pair = new PairedData(x, y);
		LineFit fit = new LineFit(pair);
		sb.append("A=").append(fit.getA()).append("\tB=").append(fit.getB());
		print(sb.toString());
	}

	public double getNetworkVertexDegreeDistributionEntropy() {
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		
		double[] degrees = new double[vertexArr.size()];
		int index = 0;
		int max = 0;
		for(Number vertex : vertexArr) {
			degrees[index] = network.getNeighborCount(vertex);
			if(max<degrees[index]) {
				max = (int) degrees[index];
			}
			index++;
		}
		
		return DiscreteDistribution.entropy(degrees);
	}


    /********************************************************************************
     * Vertex properties
     ********************************************************************************/
	public HashMap<Number, Integer> getVertexDegrees() {
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		HashMap<Number, Integer> degreeMap = new HashMap<Number, Integer>();
		
		if(printDetail) {
			StringBuilder sb = new StringBuilder();
			for(Number vertex : vertexArr) {
				int degree = network.getNeighborCount(vertex);
				degreeMap.put(vertex, degree);
				sb.append(vertex).append("\t").append(degree).append("\n");
			}
			print(sb.toString());
		}
		
		return degreeMap;
	}

	public void getVertexClusteringCoefficient() {
		Map<Number,Double> map = Metrics.clusteringCoefficients(network);
		StringBuilder sb = new StringBuilder();
		for(Number vertex : map.keySet()) {
			sb.append(vertex).append("\t").append(formatDecimal.format(map.get(vertex))).append("\n");
		}
		print(sb.toString());
	}

	public void getVertexEigenCentrality() {
		EigenvectorCentrality ranker = new EigenvectorCentrality(network);
		ranker.evaluate();

		double size = vertexList.size();
		StringBuilder sb = new StringBuilder();
		sb.append("network vertex eigen centrality:\n");
		for(Number vertex : vertexList) {
			double score = (Double)ranker.getVertexScore(vertex) * size;
			sb.append(vertex + "\t" + formatDecimal.format(score) + "\n");
		}
		
		print(sb.toString());
	}

	public void getVertexBetweenness() {
		BetweennessCentrality ranker = new BetweennessCentrality(network, true, false);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
//		ranker.printRankings(true, true);

		StringBuilder sb = new StringBuilder();
		sb.append("network vertex betweenness:\n");
//		double size = vertexList.size();
//		double normValue = (size-1)*(size-2)/2;
		for(Number vertex : vertexList) {
			double score = ranker.getVertexRankScore(vertex);//normValue;
			sb.append(vertex + "\t" + formatDecimal.format(score) + "\n");
		}
		print(sb.toString());
	}

	public void getVertexDegreeCorrelation() {
		StringBuilder sb = new StringBuilder();
		sb.append("network vertex degree correlation:\n");
		for(Number vertex : vertexList) {
			int degreeTotal = 0;
			int count = 0;
			ArrayList<Number> neighborList = new ArrayList<Number>(network.getNeighbors(vertex));
			for(Number neigbor : neighborList) {
				int degree = network.getNeighborCount(neigbor);
				if(degree>1) {
					degreeTotal += degree;
					count++;
				}
			}
			sb.append(vertex + "\t" + network.getNeighborCount(vertex) + "\t");
			if(count>0) {
				sb.append(formatDecimal.format((double)degreeTotal/count) + "\n");
			} else {
				sb.append("0\n");
			}
		}
		
		print(sb.toString());		
	}

	public void getVertexClusterCoef() {
		StringBuilder sb = new StringBuilder();
		sb.append("network vertex cluster coefficient:\n");
		Map<Number, Double> coefMap = Metrics.clusteringCoefficients(network);
		
		for(Number vertex : vertexList) {
			sb.append(vertex + "\t" + formatDecimal.format(coefMap.get(vertex)) + "\n");
		}

		print(sb.toString());	
	}

	public void getVertexCloseness() {
		StringBuilder sb = new StringBuilder();
		sb.append("network vertex closeness:\n");
		UnweightedShortestPath shortestPath = new UnweightedShortestPath(network);
		for(Number vertex : vertexList) {
			Map<Number, Number> distanceMap = shortestPath.getDistanceMap(vertex);
			double average = 0;
			int count = 0;
			for(Number vertex2 : vertexList) {
				Number distance = distanceMap.get(vertex2);
				if(distance!=null) {
					count++;
					average += (Integer)(distanceMap.get(vertex2)).intValue();
				}
			}
			sb.append(vertex + "\t" + formatDecimal.format(average/count) + "\n");
		}
		
		print(sb.toString());		
	}

	public void getVertexNeighborDegreeDistribution(int degreeLow, int degreeHigh) {
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		int frequencySize = 2;
		HashMap<Integer, Integer> refernceDistribution = getNetworkVertexDegreeDistribution(frequencySize);
		for(int i=vertexArr.size()-1; i>=0; i--) {
			Number vertex = vertexArr.get(i);
			int neighborCount = network.getNeighborCount(vertex);
			if(neighborCount>=degreeLow && neighborCount<=degreeHigh) {
				HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();
				for(Number neighbor : network.getNeighbors(vertex)) {
					int degreeNeighbor = network.getNeighborCount(neighbor) / frequencySize;
					Integer count = distribution.get(degreeNeighbor);
					if(count!=null) {
						distribution.put(degreeNeighbor, count.intValue()+1);
					} else {
						distribution.put(degreeNeighbor, 1);
					}
				}
				ArrayList<Integer> indexArr = new ArrayList<Integer>();
				indexArr.addAll(distribution.keySet());
				Collections.sort(indexArr);
				int total = 0;
				int max = 0;
				int maxIndex = 0;
				for(int index : indexArr) {
					int count = distribution.get(index);
					total += count;
					if(max<count) {
						max = count;
						maxIndex = index;
					}
				}
				StringBuilder sb = new StringBuilder();
//				sb.append(yaoNameIndex.getNameByIndex(vertex.intValue())).append("(").append(i).append("\t").append(neighborCount).append("):\n");
				sb.append(i).append("\t").append(neighborCount).append("\t").append(maxIndex*frequencySize);
//				for(int index : indexArr) {
//					int count = distribution.get(index);
//					sb.append(index).append("\t").append(count)
//					.append("\t").append(numberFormat.format((count*10.0)/refernceDistribution.get(index)))
//					.append("\t").append(numberFormat.format((count*1.0)/total))
//					.append("\n"); 
//				}
				System.out.println(sb);
			}
		}
	}

	public double getVertexEdgeDegrees(int valueType) {
		HashMap<Number, ArrayList<Integer>> vertexEdgeDegree = new HashMap<Number, ArrayList<Integer>>();
		for(Number edge : edgeList) {
			Pair<Number> pair = network.getEndpoints(edge);
			Number end1 = pair.getFirst();
			Number end2 = pair.getSecond();
			
			HashSet<Number> neighborEnd1 = new HashSet<Number>(network.getNeighbors(end1));
			HashSet<Number> neighborEnd2 = new HashSet<Number>(network.getNeighbors(end2));
			neighborEnd1.retainAll(neighborEnd2);
			int degree = neighborEnd1.size();
			
			ArrayList<Integer> edgeSetEnd1 = vertexEdgeDegree.get(end1);
			if(edgeSetEnd1==null) {
				edgeSetEnd1 = new ArrayList<Integer>();
				 vertexEdgeDegree.put(end1, edgeSetEnd1);
			}
			edgeSetEnd1.add(degree);
			
			ArrayList<Integer> edgeSetEnd2 = vertexEdgeDegree.get(end2);
			if(edgeSetEnd2==null) {
				edgeSetEnd2 = new ArrayList<Integer>();
				 vertexEdgeDegree.put(end2, edgeSetEnd2);
			}
			edgeSetEnd2.add(degree);
		}
		
		StringBuilder sb = new StringBuilder();
		double totalAllVertex = 0;
		if(valueType == DISTRIBUTION_VALUE_TYPE_AVERAGE) {
//			sb.append("vertex average edge degrees:\n");
			for(Number vertex : vertexList) {
				sb.append(vertex).append("\t").append(network.getNeighbors(vertex).size()).append("\t");
				ArrayList<Integer> edgeSetEnd = vertexEdgeDegree.get(vertex);
				if(edgeSetEnd!=null && edgeSetEnd.size()>0) {
					double total = 0.0;
					for(int degree : edgeSetEnd) {
						total += degree;
					}
					totalAllVertex += total/edgeSetEnd.size();
					sb.append(formatDecimal.format(total/edgeSetEnd.size())).append("\n");
				} else {
					sb.append("-1\n");
				}
			}
		} else if(valueType == DISTRIBUTION_VALUE_TYPE_MODE) {
////			sb.append("vertex mode edge degrees:\n");
			for(Number vertex : vertexList) {
				sb.append(vertex).append("\t").append(network.getNeighbors(vertex).size()).append("\t");
				ArrayList<Integer> edgeSetEnd = vertexEdgeDegree.get(vertex);
				HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();
				if(edgeSetEnd!=null && edgeSetEnd.size()>0) {
					int maxCount = 0;
					int maxIndex = 0;
					for(int degree : edgeSetEnd) {
						int count = distribution.containsKey(degree) ? distribution.get(degree) : 0;
						count++;
						distribution.put(degree, count);
						if(maxCount<count) {
							maxCount = count; 
							maxIndex = degree;
						}
					}
					totalAllVertex += maxIndex;
					sb.append(maxIndex).append("\n");
				} else {
					sb.append("-1\n");
				}
			}
		} else if(valueType == DISTRIBUTION_VALUE_TYPE_MAX) {
////		sb.append("vertex max edge degrees:\n");
			for(Number vertex : vertexList) {
				sb.append(vertex).append("\t").append(network.getNeighbors(vertex).size()).append("\t");
				ArrayList<Integer> edgeSetEnd = vertexEdgeDegree.get(vertex);
				Collections.sort(edgeSetEnd);
				totalAllVertex += edgeSetEnd.get(edgeSetEnd.size()-1);
				sb.append(edgeSetEnd.get(edgeSetEnd.size()-1)).append("\n");
			}
			
		} else if(valueType == DISTRIBUTION_VALUE_TYPE_MIN) {
////		sb.append("vertex min edge degrees:\n");
			for(Number vertex : vertexList) {
				sb.append(vertex).append("\t").append(network.getNeighbors(vertex).size()).append("\t");
				ArrayList<Integer> edgeSetEnd = vertexEdgeDegree.get(vertex);
				Collections.sort(edgeSetEnd);
				totalAllVertex += edgeSetEnd.get(0);
				sb.append(edgeSetEnd.get(0)).append("\n");
			}
		}
		
		double avgValue = totalAllVertex/vertexList.size();
		sb.append("Average = ").append(formatDecimal.format(avgValue));
		print(sb.toString());
		
		return avgValue;
	}

	public double getVertexComponentDegreeValueWithout1() {
		HashMap<Number, Integer> degreeMap = getVertexComponentDegrees();
		double total = 0;
		int count = 0;
		for(int value : degreeMap.values()) {
			if(value>1) {
				total += value;
				count++;
			}
		}
		System.out.println(count + "\t" + formatDecimal.format(total/count));

		if(count>0) {
			return total / count;
		} else {
			return -1;
		}
	}
	
	public HashMap<Number, Integer> getVertexComponentDegrees() {
		StringBuilder sb = new StringBuilder();
		HashMap<Number, Integer> degreeMap = new HashMap<Number, Integer>();
		for(Number vertex : vertexList) {
//			int degree = getVertexComponentDegree2(vertex);	//slow
			int degree = getVertexComponentDegree(vertex);
			degreeMap.put(vertex, degree);
			if(printDetail) {
				sb.append(vertex).append("\t").append(degree)
				.append("\t").append(formatDecimal.format(((double)degree)/network.getNeighborCount(vertex)))
				.append("\n");
			}
		}
		if(printDetail) {
			sb.insert(0, "id\tcomponent degree\tcomponent degree normalized\n");
			print(sb.toString());
		}
		
		return degreeMap;
	}

	public int getVertexComponentDegree2(Number vertex) {
		HashSet<Number> neighborSet = new HashSet<Number>(network.getNeighbors(vertex));
		HashSet<Number> leftSet = new HashSet<Number>();
		leftSet.addAll(neighborSet);
		ArrayList<HashSet<Number>> componentList = new ArrayList<HashSet<Number>>();
		for(Number neighbor : neighborSet) {
			if(leftSet.contains(neighbor)) {
				int leftCount = leftSet.size();
				HashSet<Number> set = new HashSet<Number>();
				componentList.add(set);
				set.add(neighbor);
				leftSet.remove(neighbor);
				while(leftCount>leftSet.size()) {
					leftCount = leftSet.size();
					HashSet<Number> setNeighbors = new HashSet<Number>();
					for(Number v : set) {
						setNeighbors.addAll(network.getNeighbors(v));
					}
					setNeighbors.retainAll(leftSet);
					if(setNeighbors.size()>0) {
						set.addAll(setNeighbors);
						leftSet.removeAll(setNeighbors);
					}
				}
				if(leftSet.size()==0) {
					break;
				}
			}
		}
		
		return componentList.size();
	}

	public int getVertexComponentDegree(Number vertex)  {
		int degree = 0;
		HashSet<Number> neighborSet = new HashSet<Number>(network.getNeighbors(vertex));
		HashSet<Number> usedSet = new HashSet<Number>();
		HashSet<Number> leftSet = new HashSet<Number>();
		leftSet.addAll(neighborSet);
		for(Number neighbor : neighborSet) {
			if(!usedSet.contains(neighbor)) {
				degree++;
				HashSet<Number> componentSet = getConnectedComponent(neighbor, leftSet);
				usedSet.addAll(componentSet);
				leftSet.removeAll(componentSet);
			}
		}
		
		return degree;
	}

	protected HashSet<Number> getConnectedComponent(Number vertex, HashSet<Number> scopeSet) {
		HashSet<Number> neighborSet = new HashSet<Number>(network.getNeighbors(vertex));
		neighborSet.retainAll(scopeSet);
		HashSet<Number> leftSet = new HashSet<Number>(scopeSet);
		leftSet.removeAll(neighborSet);
		leftSet.remove(vertex);
		if(leftSet.size()>0) {
			for(Number neighbor : neighborSet) {
				HashSet<Number> set = getConnectedComponent(neighbor, leftSet);
				leftSet.removeAll(set);
				if(leftSet.size()==0) {
					break;
				}
			}
		}
		
		HashSet<Number> componentSet = new HashSet<Number>(scopeSet);
		componentSet.removeAll(leftSet);
		componentSet.add(vertex);
		return componentSet;
	}


    /********************************************************************************
     * Edge properties
     ********************************************************************************/
	public void getEdgeBetweenness() {

		BetweennessCentrality ranker = new BetweennessCentrality(network, true,true);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
//		ranker.printRankings(true, true);

		StringBuilder sb = new StringBuilder();
		sb.append("network vertex betweenness:\n");
		
		sb.append("\nnetwork edges betweenness:\n");
		for(Number edge : edgeList) {
			double score = ranker.getEdgeRankScore(edge);
			sb.append(edge + "\t" + formatDecimal.format(score) + "\t" + network.getEndpoints(edge) + "\n");
		}
		
		print(sb.toString());
	}

	public double getEdgeDegrees(boolean normal) {
		StringBuilder sb = new StringBuilder();
		sb.append("edge degrees:\nid\tdegree\tmax\tnormal\n");
		double total = 0;
		double totalNormal = 0;
		for(Number edge : edgeList) {
			Pair<Number> pair = network.getEndpoints(edge);
			Number end1 = pair.getFirst();
			Number end2 = pair.getSecond();
			
			HashSet<Number> neighborEnd1 = new HashSet<Number>(network.getNeighbors(end1));
			neighborEnd1.remove(end2);
			HashSet<Number> neighborEnd2 = new HashSet<Number>(network.getNeighbors(end2));
			neighborEnd2.remove(end1);
			neighborEnd1.retainAll(neighborEnd2);
			int degree = neighborEnd1.size();
			int maxDegree = degree + neighborEnd2.size(); 
			double normalValue = 1; 
			if(maxDegree>0) {
				normalValue = degree*2.0/maxDegree;
			}
				
			sb.append(edge)
			.append("\t").append(degree)
			.append("\t").append(maxDegree)
			.append("\t").append(formatDecimal.format(normalValue)).append("\n");
			total += degree;
			totalNormal += normalValue;
		}
		
		int edgeCount = edgeList.size();
		int vertexCount = vertexList.size();
		double density = edgeCount*2.0/(vertexCount*(vertexCount-1));
		
		sb.append("Average = ").append(formatDecimal.format(total/edgeCount))
			.append("\nAverage Normalized = ").append(formatDecimal.format(totalNormal/edgeCount))
			.append("\nAverage By Vertex = ").append(formatDecimal.format(total/vertexCount))
			.append("\nAverage Normalized By Vertex = ").append(formatDecimal.format(totalNormal/vertexCount))
			.append("\nAverage By Edge Density = ").append(formatDecimal.format(total/density))
			.append("\nAverage Normalized By Edge Density = ").append(formatDecimal.format(totalNormal/density));
		print(sb.toString());
		
		if(normal) {
			return totalNormal / edgeList.size();
		} else {
			return total / edgeList.size();
		}
	}

	public void removeByEdgeDegrees(int size) {
		for(Number edge : edgeList) {
			Pair<Number> pair = network.getEndpoints(edge);
			Number end1 = pair.getFirst();
			Number end2 = pair.getSecond();
			
			HashSet<Number> neighborEnd1 = new HashSet<Number>(network.getNeighbors(end1));
			neighborEnd1.remove(end2);
			HashSet<Number> neighborEnd2 = new HashSet<Number>(network.getNeighbors(end2));
			neighborEnd2.remove(end1);
			neighborEnd1.retainAll(neighborEnd2);
			int degree = neighborEnd1.size();
			if(degree>size) {
				network.removeEdge(edge);
			}
		}
	}


	
    /********************************************************************************
     * Clustering
     ********************************************************************************/
	public void clusterByLowDegreeVertex(int degree, int numCluster) {
		HashMap<String, HashSet<String>> clusterVertex = new HashMap<String, HashSet<String>>();
		for(Number vertex : vertexList) {
			Collection<Number> neighbors = network.getNeighbors(vertex);
			if(neighbors.size()>degree) {
				String vertexName = vertexNameIndex.getNameByIndex(vertex.intValue());
				HashSet<String> neighborSet = new HashSet<String>();
				clusterVertex.put(vertexName, neighborSet);
				StringBuilder sb = new StringBuilder();
				sb.append(vertexName).append(": ");
				for(Number v1 : neighbors) {
					if(network.getNeighbors(v1).size()<=degree) {
						String neighborName = vertexNameIndex.getNameByIndex(v1.intValue());
						neighborSet.add(neighborName);
						sb.append(neighborName).append(", ");
					}
				}
//				System.out.println(sb.toString());
			}
		}
		
		ArrayList<String> vertexList = new ArrayList<String>();
		vertexList.addAll(clusterVertex.keySet());
		int count = vertexList.size();
		double[][] distMatrix = new double[count][count];
		for(int i=0; i<count; i++) {
			distMatrix[i][i] = 0;
			HashSet<String> featureV1 = clusterVertex.get(vertexList.get(i));
			for(int j=i+1; j<count; j++) {
				HashSet<String> featureV2 = clusterVertex.get(vertexList.get(j));
				HashSet<String> setDiff = new HashSet<String>();
				setDiff.addAll(featureV1);
				setDiff.retainAll(featureV2);
				HashSet<String> setCombine = new HashSet<String>();
				setCombine.addAll(featureV1);
				setCombine.addAll(featureV2);
				if(setCombine.size()==0) {
					distMatrix[i][j] = 1;
				} else {
					distMatrix[i][j] = 1 - ((double)setDiff.size())/setCombine.size();
				}
				distMatrix[j][i] = distMatrix[i][j];
			}
		}
		//saveDistance();
		
		Hierarchical cluster = new Hierarchical(Hierarchical.ALOGRITHM_WARD);
		cluster.build(distMatrix);
		int[] clusters = cluster.cluster(numCluster, true);
		System.out.println(cluster.getScatterWithinClass() + "\t" + cluster.getScatterBetweenClass());
//		System.out.println("Clusters:");
//		for(int i : clusters) {
//			int[] vertexIndexArr = cluster.listClusterData(i);	
//			StringBuilder sb = new StringBuilder();
//			sb.append("cluster ").append(i).append(":\n");
//			for(int j : vertexIndexArr) {
//				String vertex = vertexList.get(j);
//				sb.append(vertex).append(":\t");
//				sb.append(clusterVertex.get(vertex)).append("\n");
//			}
//			System.out.println(sb.toString());
//		}
		
	}

	public void clusterByNeighbor(int numCluster) {
		HashMap<String, HashSet<String>> clusterVertex = new HashMap<String, HashSet<String>>();
		for(Number vertex : vertexList) {
			Collection<Number> neighbors = network.getNeighbors(vertex);
			String vertexName = vertexNameIndex.getNameByIndex(vertex.intValue());
			HashSet<String> neighborSet = new HashSet<String>();
			clusterVertex.put(vertexName, neighborSet);
			StringBuilder sb = new StringBuilder();
			sb.append(vertexName).append(": ");
			for(Number v1 : neighbors) {
				String neighborName = vertexNameIndex.getNameByIndex(v1.intValue());
				neighborSet.add(neighborName);
				sb.append(neighborName).append(", ");
			}
//				System.out.println(sb.toString());
		}
		
		ArrayList<String> vertexList = new ArrayList<String>();
		vertexList.addAll(clusterVertex.keySet());
		int count = vertexList.size();
		double[][] distMatrix = new double[count][count];
		for(int i=0; i<count; i++) {
			distMatrix[i][i] = 0;
			HashSet<String> featureV1 = clusterVertex.get(vertexList.get(i));
			for(int j=i+1; j<count; j++) {
				HashSet<String> featureV2 = clusterVertex.get(vertexList.get(j));
				HashSet<String> setDiff = new HashSet<String>();
				setDiff.addAll(featureV1);
				setDiff.retainAll(featureV2);
				HashSet<String> setCombine = new HashSet<String>();
				setCombine.addAll(featureV1);
				setCombine.addAll(featureV2);
				if(setCombine.size()==0) {
					distMatrix[i][j] = 1;
				} else {
					distMatrix[i][j] = 1 - ((double)setDiff.size())/setCombine.size();
				}
				distMatrix[j][i] = distMatrix[i][j];
			}
		}
		//saveDistance();
		
		Hierarchical cluster = new Hierarchical(Hierarchical.ALOGRITHM_WARD);
		cluster.build(distMatrix);
		int[] clusters = cluster.cluster(numCluster, true);
		System.out.println(cluster.getScatterWithinClass() + "\t" + cluster.getScatterBetweenClass());
//		System.out.println("Clusters:");
//		for(int i : clusters) {
//			int[] vertexIndexArr = cluster.listClusterData(i);	
//			StringBuilder sb = new StringBuilder();
//			sb.append("cluster ").append(i).append(":\n");
//			for(int j : vertexIndexArr) {
//				String vertex = vertexList.get(j);
//				sb.append(vertex).append(":\t");
//				sb.append(clusterVertex.get(vertex)).append("\n");
//			}
//			System.out.println(sb.toString());
//		}
		
	}

	
    /********************************************************************************
     * Research processes
     ********************************************************************************/
	public HashSet<Number> removeVertexFromHighestDegree(int count) {
		return removeVertexByDegreeByCount(count, true);
	}
	
	public HashSet<Number> removeVertexFromHighestDegree(int indexFrom, int indexTo) {
		return removeVertexByDegreeByCount(indexFrom, indexTo, true);
	}
	
	public HashSet<Number> removeVertexFromLowestDegree(int count) {
		return removeVertexByDegreeByCount(count, false);
	}
	
	public HashSet<Number> removeVertexFromLowestDegree(int indexFrom, int indexTo) {
		return removeVertexByDegreeByCount(indexFrom, indexTo, false);
	}
	
	protected HashSet<Number> removeVertexByDegreeByCount(int count, boolean isReverse) {
		return removeVertexByDegreeByCount(0, count-1, isReverse);
	}

	protected HashSet<Number> removeVertexByDegreeByCount(int indexFrom, int indexTo, boolean isReverse) {
		HashSet<Number> removedSet = new HashSet<Number>();
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		if(isReverse) {
			Collections.reverse(vertexArr);
		}
		
		for(int i=indexFrom; i<=indexTo && i<vertexArr.size(); i++) {
			Number vertex = vertexArr.get(i);
			removedSet.add(vertex);
			network.removeVertex(vertex);
		}
		refreshNetwork();
		
		return removedSet;
	}

	protected HashSet<Number> removeVertexByDegreeByCountWithReserve(int indexFrom, int indexTo, boolean isReverse, HashSet<Number> reserveSet) {
		HashSet<Number> removedSet = new HashSet<Number>();
		ArrayList<Number> vertexArr = new ArrayList<Number>();
		vertexArr.addAll(vertexList);
		Collections.sort(vertexArr, degreeComparator);
		if(isReverse) {
			Collections.reverse(vertexArr);
		}
		
		for(int i=indexFrom; i<=indexTo && i<vertexArr.size(); i++) {
			Number vertex = vertexArr.get(i);
			if(!reserveSet.contains(vertex)) {
				removedSet.add(vertex);
				network.removeVertex(vertex);
			}
		}
		refreshNetwork();
		
		return removedSet;
	}

	protected HashSet<Number> removeVertexByDegree(int degree, boolean isBelow) {
		HashSet<Number> removedSet = new HashSet<Number>();
		for(Number vertex : vertexList) {
			if(isBelow && network.getNeighborCount(vertex) <= degree ) {
				removedSet.add(vertex);
				network.removeVertex(vertex);
			} else if(!isBelow && network.getNeighborCount(vertex) >= degree ) {
				removedSet.add(vertex);
				network.removeVertex(vertex);
			}
		}
		refreshNetwork();
		
		return removedSet;
	}
	
	public HashSet<Number> removeVertexByDegreeBelow(int degree) {
		return removeVertexByDegree(degree, true);
	}
	
	public HashSet<Number> removeVertexByDegreeAbove(int degree) {
		return removeVertexByDegree(degree, false);
	}
	
	public HashSet<Number> removeEdgeByVertexDegreeAbove(int degree) {
		HashSet<Number> removedEdges = new HashSet<Number>();
		for(Number edge : edgeList) {
			Pair<Number> vertexPair = network.getEndpoints(edge);
			Number vertex0 = vertexPair.getFirst();
			Number vertex1 = vertexPair.getSecond();
			if(network.getNeighborCount(vertex0)>degree && network.getNeighborCount(vertex1)>degree) {
				network.removeEdge(edge);
				removedEdges.add(edge);
			}
		}
		refreshNetwork();
		return removedEdges;
	}

	public List<Number> removeEdgeByBetweenness(int numOfRemoveEdge, boolean oneStep) {
		int step = 10;
		if(oneStep) {
			step = numOfRemoveEdge<edgeList.size() ? numOfRemoveEdge : edgeList.size();
		}
		
		List<Number> allRemovedEdges = new ArrayList<Number>();
		for(int i=0; i<numOfRemoveEdge && i<edgeList.size(); i+=step) {
			EdgeBetweennessClusterer cluster = new EdgeBetweennessClusterer(step);
			Set<Set<Number>> componentSet = cluster.transform(network);
//			System.out.println("remove " + i + ", component count:\t" + componentSet.size());
			System.out.println(i + "\t" + componentSet.size());
			List<Number> removedEdges = cluster.getEdgesRemoved();
			for(Number edge : removedEdges) {
				network.removeEdge(edge);
			}
			allRemovedEdges.addAll(removedEdges);
		}

		return allRemovedEdges;
	}
	
	public List<Number> removeEdgeByBetweenness(int numOfRemovedEdge) {
		return removeEdgeByBetweenness(numOfRemovedEdge, true);
	}

	public int getGCSize() {
		WeakComponentClusterer cluster = new WeakComponentClusterer();
		Set<Set<Number>> componentSet = cluster.transform(network);
		ArrayList<Set<Number>> componentArr = new ArrayList<Set<Number>>();
		componentArr.addAll(componentSet);
		Collections.sort(componentArr, new Comparator<Set>() {
		    public int compare(Set o1, Set o2) {
		    	return o2.size() - o1.size();
		    }});
		
		if(printDetail) {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(componentArr.size()).append(")");
			for(Set component : componentArr) {
				sb.append(component.size()).append(", ");
			}
			print(sb.toString());
		}
		return componentArr.get(0).size();
	}

	public void focusOnGC() {
		WeakComponentClusterer cluster = new WeakComponentClusterer();
		Set<Set<Number>> componentSet = cluster.transform(network);

		Set<Number> maxSet = new HashSet<Number>();
		for(Set<Number> set : componentSet) {
			if(maxSet.size()<set.size()) {
				if(maxSet.size()>0) {
					for(Number vertex : maxSet) {
						network.removeVertex(vertex);
					}
				}
				maxSet = set;
			} else {
				for(Number vertex : set) {
					network.removeVertex(vertex);
				}
			}
		}
		
		refreshNetwork();
	}
	
	public void getLargestClique(boolean remove) {
		ArrayList<Number> vertexClique = new ArrayList<Number>();
		HashSet<Number> commonNeighbor = new HashSet<Number>();
		commonNeighbor.addAll(vertexList);
		while(true) {
			int maxCommonCount = 0;
			Number maxIndex = -1;
			for(Number vertex : commonNeighbor) {
				if(!vertexClique.contains(vertex)) {
					HashSet<Number> neighbors = new HashSet<Number>();
					neighbors.addAll(network.getNeighbors(vertex));
					neighbors.retainAll(commonNeighbor);
					if(maxCommonCount<neighbors.size()) {
						maxCommonCount=neighbors.size();
						maxIndex = vertex;
					}
				}
			}
			if(maxIndex.intValue()==-1) {
				break;
			} else {
				commonNeighbor.retainAll(network.getNeighbors(maxIndex));
				vertexClique.add(maxIndex);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(Number vertex : vertexClique) {
			if(remove) {
				network.removeVertex(vertex);
			}
			int index = vertex.intValue();
			sb.append(vertexNameIndex.getNameByIndex(index) + "(" + vertex + ")\n");
		}
		print(sb.toString());
	}

	public void removeByVertexComponentDegreesLowest(int degree, HashMap<Number, Integer> degreeMap) {
		removeByVertexComponentDegreeRange(0, degree, degreeMap);
	}

	public void removeByVertexComponentDegreesLowest(int degree) {
		HashMap<Number, Integer> degreeMap = getVertexComponentDegrees();
		removeByVertexComponentDegreesLowest(degree, degreeMap);
	}

	public void removeByVertexComponentDegreesHighest(int degree, HashMap<Number, Integer> degreeMap) {
		removeByVertexComponentDegreeRange(degree, Integer.MAX_VALUE, degreeMap);
	}

	public void removeByVertexComponentDegreesHighest(int degree) {
		HashMap<Number, Integer> degreeMap = getVertexComponentDegrees();
		removeByVertexComponentDegreesHighest(degree, degreeMap);
	}

	public void removeByVertexComponentDegreeRange(int degreeMin, int degreeMax, HashMap<Number, Integer> degreeMap) {
		for(Number vertex : degreeMap.keySet()) {
			int degree = degreeMap.get(vertex);
			if(degree>=degreeMin && degree<=degreeMax) {
				network.removeVertex(vertex);
			}
		}
		refreshNetwork();
	}

	//This is not a correct one because dynmaic remove a vertex will change the properties of the rest vertices
	public void removeByVertexComponentDegreesLowest2(int degree) {
		for(Number vertex : vertexList) {
			if(getVertexComponentDegree(vertex)<=1) {
				network.removeVertex(vertex);
			}
		}
		refreshNetwork();
	}

	public HashMap<Number, HashSet<Number>> condenseNetworkByVertexComponentDegree(int degreeThreshold) {
		HashMap<Number, HashSet<Number>> condensedComponents = new HashMap<Number, HashSet<Number>>();
		
		for(Number vertex : vertexList) {
			if(network.containsVertex(vertex)) {
				int degree = getVertexComponentDegree(vertex);
				if(degree<=degreeThreshold) {
					HashSet<Number> componentSet = new HashSet<Number>();
					condensedComponents.put(vertex, componentSet);
					componentSet.add(vertex);
					HashSet<Number> neighborSet = new HashSet<Number>(network.getNeighbors(vertex));
//					System.out.print(vertexNameIndex.getNameByIndex(vertex.intValue()) + ", ");
					for(Number neighbor : neighborSet) {
						if(getVertexComponentDegree(neighbor)<=degreeThreshold) {
//							System.out.print(vertexNameIndex.getNameByIndex(neighbor.intValue()) + ", ");
							componentSet.add(neighbor);
							mergeVertex(vertex, neighbor);
						}
					}
//					System.out.println();
				}
			}
		}
		refreshNetwork();
		return condensedComponents;
	}

	public void mergeVertex(Number v1, Number v2) {
		HashSet<Number> v1NeighborSet = new HashSet<Number>(network.getNeighbors(v1));
		HashSet<Number> v2NeighborSet = new HashSet<Number>(network.getNeighbors(v2));
		v2NeighborSet.removeAll(v1NeighborSet);
		v2NeighborSet.remove(v1);
		for(Number neigbhorNeighbor : v2NeighborSet) {
			Number edge = network.findEdge(v2, neigbhorNeighbor);
			network.removeEdge(edge);
			network.addEdge(edge, v1, neigbhorNeighbor, EdgeType.UNDIRECTED);
		}
		network.removeVertex(v2);
	}

}
