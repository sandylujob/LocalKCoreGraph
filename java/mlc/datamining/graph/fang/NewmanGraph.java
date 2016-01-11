package mlc.datamining.graph.fang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class NewmanGraph extends BaseGraph {

	public NewmanGraph() {
		super();
	}
	
	public NewmanGraph(String dir, String file) throws Exception {
		this.workingDir = dir;
		this.inputFile = file;
		buildNetwork();
	}

	public void rebuild() throws Exception {
		network = new UndirectedSparseGraph();
		buildNetwork();
	}

	protected void buildNetwork() throws Exception {
    	File file = new File(workingDir+inputFile);
		if(file.exists()) {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			String line = null; 
			while ((line = input.readLine()) != null){
				if(line.trim().equalsIgnoreCase("node")) {
					input.readLine();
					//id line
					StringTokenizer st = new StringTokenizer(input.readLine(), " ");
					st.nextToken();
					String id = st.nextToken();
					//label line
					st = new StringTokenizer(input.readLine(), "\" ");
					st.nextToken();
					String label = st.nextToken();
					vertexNameIndex.add(Integer.parseInt(id), label);
					network.addVertex(Integer.parseInt(id));
				} else if(line.trim().equalsIgnoreCase("edge")) {
					input.readLine();
					StringTokenizer st = new StringTokenizer(input.readLine(), " ");
					st.nextToken();
					String idSource = st.nextToken();
					st = new StringTokenizer(input.readLine(), " ");
					st.nextToken();
					String idDest = st.nextToken();
					network.addEdge(network.getEdgeCount()+1, Integer.parseInt(idSource), Integer.parseInt(idDest), EdgeType.UNDIRECTED);
				}
			}
	    	input.close();
		}
		refreshNetwork();
	}


	public static void main(String[] args) throws Exception 
	{
		System.out.println(new Date());
		NewmanGraph graph = new NewmanGraph();
		graph.workingDir = "C:/Users/sandy/Documents/MATLAB/javagraph/data/newman/";
		graph.inputFile = "football.gml";
		
		graph.buildNetwork();
		System.out.println(new Date());
	}


}
