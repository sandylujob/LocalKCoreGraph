/**
 * Copyright Â© 2000-2008 MedPlus, Inc. All rights reserved.
 */
package mlc.datamining.graph;

/**
 * @author http://algowiki.net/wiki/index.php/Edge
 *
 */
public class Edge implements Comparable<Edge> {

	   Node from, to;
	   int weight;

	   public Edge(Node f, Node t, int w){
	       from = f;
	       to = t;
	       weight = w;
	   }

	   public int compareTo(Edge e){
	       return weight - e.weight;
	   }
	}
