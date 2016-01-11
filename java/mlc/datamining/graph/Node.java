/**
 * Copyright Â© 2000-2008 MedPlus, Inc. All rights reserved.
 */
package mlc.datamining.graph;

/**
 * @author http://algowiki.net/wiki/index.php/Edge
 *
 */
public class Node implements Comparable<Node> {

	   int name;
	   boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
	   int lowlink = -1;          // used for Tarjan's algorithm
	   int index = -1;            // used for Tarjan's algorithm
	   
	   public Node(int n){
	       name = n;
	   }

	   public int compareTo(Node n){
	       if(n == this)
	           return 0;
	       return -1;
	   }
	}
