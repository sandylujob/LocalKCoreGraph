/**
 * Copyright Â© 2000-2008 MedPlus, Inc. All rights reserved.
 */
package mlc.datamining.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author http://algowiki.net/wiki/index.php/Edge
 *
 */
public class FloydWarshall {

	int[][] D;
	Node[][] P;

	public void calcShortestPaths(Node[] nodes, Edge[] edges) {
       D = initializeWeight(nodes, edges);
       P = new Node[nodes.length][nodes.length];

       for(int k=0; k<nodes.length; k++){
    	   if(k%100==0) {
    		   System.out.println(k + " " + new Date());
    	   }
           for(int i=0; i<nodes.length; i++){
               for(int j=0; j<nodes.length; j++){
                   if(D[i][k] != Integer.MAX_VALUE && D[k][j] != Integer.MAX_VALUE && D[i][k]+D[k][j] < D[i][j]){
                       D[i][j] = D[i][k]+D[k][j];
                       P[i][j] = nodes[k];
                   }
               }
           }
       }
	}

	public int getShortestDistance(Node source, Node target){
       return D[source.name][target.name];
	}

	public ArrayList<Node> getShortestPath(Node source, Node target){
       if(D[source.name][target.name] == Integer.MAX_VALUE){
           return new ArrayList<Node>();
       }
       ArrayList<Node> path = getIntermediatePath(source, target);
       path.add(0, source);
       path.add(target);
       return path;
	}

	private ArrayList<Node> getIntermediatePath(Node source, Node target){
       if(D == null){
           throw new IllegalArgumentException("Must call calcShortestPaths(...) before attempting to obtain a path.");
       }
       if(P[source.name][target.name] == null){
           return new ArrayList<Node>();
       }
       ArrayList<Node> path = new ArrayList<Node>();
       path.addAll(getIntermediatePath(source, P[source.name][target.name]));
       path.add(P[source.name][target.name]);
       path.addAll(getIntermediatePath(P[source.name][target.name], target));
       return path;
	}

	private int[][] initializeWeight(Node[] nodes, Edge[] edges){
       int[][] Weight = new int[nodes.length][nodes.length];
       for(int i=0; i<nodes.length; i++){
           Arrays.fill(Weight[i], Integer.MAX_VALUE);
       }
       for(Edge e : edges){
           Weight[e.from.name][e.to.name] = e.weight;
       }
       return Weight;
	}

	/**
	 * @return the d
	 */
	public int[][] getD() {
		return D;
	}

	/**
	 * @param d the d to set
	 */
	public void setD(int[][] d) {
		D = d;
	}

	/**
	 * @return the p
	 */
	public Node[][] getP() {
		return P;
	}

	/**
	 * @param p the p to set
	 */
	public void setP(Node[][] p) {
		P = p;
	}
}
