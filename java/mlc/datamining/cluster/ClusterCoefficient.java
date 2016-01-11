package mlc.datamining.cluster;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import mlc.datamining.util.Itemset;
import mlc.datamining.util.Itemsets;
import mlc.datamining.util.NeighborNode;

/**
 * @author chen_l
 *
 */
public class ClusterCoefficient {

	public static double getCoefficient(int[][] matrix) {	//adjacency matrix
		int numOfNode = matrix.length;
		NeighborNode[] neighborNodes = new NeighborNode[numOfNode];
		for(int i=0; i<numOfNode; i++) {
			for(int j=0; j<numOfNode; j++) {
				if(matrix[i][j]==1) {
					neighborNodes[i].setId(Integer.toString(i));
					neighborNodes[i].addNeighbor(Integer.toString(j));
				}
			}
		}
		return getCoefficient(neighborNodes);
	}
	
	public static double getCoefficient(List<String> list) {	//adjacency list
		int numOfNode = list.size();
		NeighborNode[] neighborNodes = new NeighborNode[numOfNode];
		for(int i=0; i<numOfNode; i++) {
			String neighborStr = list.get(i);
			String[] neighborArr = neighborStr.split(Itemset.NON_ITEM_STRING);
			String id1 = Integer.toString(i);
			neighborNodes[i].setId(id1);
			for(String id2 : neighborArr) {
				neighborNodes[i].addNeighbor(id2);
			}
		}
		return getCoefficient(neighborNodes);
	}
	
	public static double getCoefficient(NeighborNode[] neighborNodes) {
		System.out.println("getCoefficient start: " + new Date());
		double coef = 0;
		Itemsets edges = new Itemsets();
		
		//build edges
		for(NeighborNode node : neighborNodes) {
			String id1 = node.getId();
			HashSet<String> neighbors = node.getNeighbors();
			for(String id2 : neighbors) {
				edges.add(id1 + Itemset.NON_ITEM_STRING + id2);
			}
		}
		
		//calculate coefficient for each node
		for(NeighborNode node : neighborNodes) {
			HashSet<String> neighborSet = node.getNeighbors();
			int numOfNeighbor = neighborSet.size();
			String[] neighborArr = new String[numOfNeighbor];
			neighborSet.toArray(neighborArr);
			int numOfEdge = numOfNeighbor;
			for(int i=0; i<numOfNeighbor; i++) {
				for(int j=i+1; j<numOfNeighbor; j++) {
					String id = Itemset.constructId(new String[] {neighborArr[i], neighborArr[j]});
					if(edges.contain(id)) {
						numOfEdge++;
					}
				}
			}
			double myCoef = 1;
			if(numOfEdge>0) {
				myCoef = ((double)numOfEdge+numOfEdge) / ((double)(numOfNeighbor+1)*numOfNeighbor);
			}
			System.out.println(myCoef);
			coef += myCoef;
		}
		
		System.out.println("end: " + new Date());
		return coef / neighborNodes.length;
	}

	public static double getCoefficient2(NeighborNode[] neighborNodes) {	//too slow
		System.out.println("getCoefficient2 start: " + new Date());
		double coef = 0;
		Itemsets edges = new Itemsets();
		
		//build edges
		for(NeighborNode node : neighborNodes) {
			String id1 = node.getId();
			HashSet<String> neighbors = node.getNeighbors();
			for(String id2 : neighbors) {
				edges.add(id1 + Itemset.NON_ITEM_STRING + id2);
			}
		}
		//calculate coefficient for each node
		int numOfAllEdge = edges.size();
		for(NeighborNode node : neighborNodes) {
			int numOfInsideEdge = 0;
			int numOfCutEdge = 0;
			HashSet<String> neighborSet = node.getNeighbors();
			int numOfNeighbor = neighborSet.size();
			neighborSet.add(node.getId());
			for(int i=0; i<numOfAllEdge; i++) {
				Itemset edge = edges.get(i);
				HashSet<String> tmpSet = new HashSet<String>(edge.getItems());
				tmpSet.addAll(neighborSet);
				if(tmpSet.size()==neighborSet.size()) { //contain both nodes -> inside edge
					numOfInsideEdge++;
				} else if(tmpSet.size()==neighborSet.size()+1) { //contain one node -> cut edge
					numOfCutEdge++;
				}  
			}
			double myCoef = 1;
			if(numOfInsideEdge>0) {
				myCoef = ((double)numOfInsideEdge+numOfInsideEdge) / ((double)(numOfNeighbor+1)*numOfNeighbor);
			}
			System.out.println(myCoef);
			coef += myCoef;
		}
		
		System.out.println("end: " + new Date());
		return coef / neighborNodes.length;
	}

}
