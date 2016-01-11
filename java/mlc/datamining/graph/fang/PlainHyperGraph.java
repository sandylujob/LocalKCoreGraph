package mlc.datamining.graph.fang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class PlainHyperGraph extends HyperGraph {

	String fileVertexIndex = "vertexIndex.txt";
	
	public PlainHyperGraph() {
		super();
	}

	public PlainHyperGraph(String dir, String file) throws Exception {
		this.workingDir = dir;
		this.inputFile = file;
		readData();
		buildHyperNetwork();
	}

    public void rebuild() throws Exception {
		network = new UndirectedSparseGraph();
		buildNetwork();
	}

	protected void readData() throws Exception {
    	File file = new File(workingDir+inputFile);
		if(file.exists()) {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			String line = null; 
			int indexFang = 0;
			int indexYao = 0;
			while ((line = input.readLine()) != null){
				//input format: yao1 yao2 ....
				HashSet<Integer> yaoList = new HashSet<Integer>();
				fangYao.put(indexFang, yaoList);
				StringTokenizer stYao = new StringTokenizer(line, ", ");
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
	    	input.close();
		}

		vertexNameIndex.print(workingDir+fileVertexIndex);
	}



	public static void main(String[] args) throws Exception 
	{
		
		System.out.println(new Date());

		System.out.println(new Date());
	}

}
