package mlc.datamining.util.distance;

import java.util.ArrayList;
import java.util.HashSet;

import mlc.datamining.util.NeighborNode;

public class Rock {
	public static double[][] similarity(ArrayList<NeighborNode> nodes)	//input format: mxn matrix of 1/0
	{
		int numOfNode = nodes.size();
		double[][] similarity = new double[numOfNode][numOfNode];
		int maxSimilarity = 0;
		for(int i =0; i<numOfNode; i++) {
			if(i%100==0) {
				//System.out.println(i + "/" + numOfNode + " " + new Date());
			}
			
			NeighborNode node1 = (NeighborNode)nodes.get(i);
			int neighbor1Size = node1.neighborSize();
			HashSet<String> neighbors1 = node1.getNeighbors();
			for(int j=i+1; j<numOfNode; j++) {
				NeighborNode node2 = (NeighborNode)nodes.get(j);
				int neighbor2Size = node2.neighborSize();
				HashSet<String> mergeNeighbor = new HashSet<String>(node2.getNeighbors());
				mergeNeighbor.addAll(neighbors1);
				int shareSize = neighbor1Size + neighbor2Size - mergeNeighbor.size();
				similarity[i][j] = shareSize;
				similarity[j][i] = shareSize;
				if(maxSimilarity < shareSize) {
					maxSimilarity = shareSize;
				}
			}
		}
		
		for(int i =0; i<numOfNode; i++) {
			for(int j=i+1; j<numOfNode; j++) {
				if(similarity[i][j] > 0) {
					similarity[i][j] /= maxSimilarity;
					similarity[j][i] = similarity[i][j] ;
				}
			}
		}
		
		return similarity;
	}

	public static double[][] distance(ArrayList<NeighborNode> nodes) 
	{
		double[][] similarity = similarity(nodes);
		int numOfData = similarity.length;
		double[][] distance = new double[numOfData][numOfData];
		for(int i=0; i<numOfData; i++)
		{
			for(int j=i+1; j<numOfData; j++)
			{
				distance[i][j] = 1 - similarity[i][j];
				distance[j][i] = distance[i][j];
			}
		}
		return distance;
	}
	
}
