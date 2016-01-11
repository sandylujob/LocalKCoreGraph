package mlc.datamining.cluster;

/**
 * @author chen_l
 *
 */
import java.util.*;

public class Hierarchical 
{
	public static final int ALOGRITHM_SINGLE = 0;
	public static final int ALOGRITHM_COMPLETE = 1;
	public static final int ALOGRITHM_WPGMA = 2;
	public static final int ALOGRITHM_UPGMA = 3;
	public static final int ALOGRITHM_WPGMC = 4;
	public static final int ALOGRITHM_UPGMC = 5;
	public static final int ALOGRITHM_WARD = 6;
	private int algorithm = ALOGRITHM_SINGLE;

	public static final int DISSIMILARITY = 0;
	public static final int SIMILARITY = 1;
	private int proximityType = DISSIMILARITY;
	
	private class Node	//the hierarchical tree is implemented by a array
	{
		double distance;
		int child1 = -1;
		int child2 = -1;
		int count;
	}
	private Node[] node = null;	//the last node is the root
	
	private double[][] distances = null;
	private int numOfData = 0;
	private int numOfCluster = 0;
	private int[] clusters = null;	//hold the index of node of the hierarchical tree
	private double scatterBetweenClass, scatterWithinClass;
	
	public Hierarchical() {}
	
	public Hierarchical(int algorithm) 
	{
		this.algorithm = algorithm;
	}

	public void build(double[][] distance, int type)
	{
		if(proximityType == SIMILARITY)
			build(convertSimilarityToDistance(distance));
	}
	
	public void build(double[][] distance)
	{
		if(distance==null || distance.length<2)
			return;

		storeDistance(distance);
		if(this.algorithm == ALOGRITHM_WARD)
			distance = convertToWardDistance(distance);
		
		numOfData = distance.length;
		node = new Node[2*numOfData-1];	//the first numOfData-1 nodes are the internal nodes, 
										//the rest of numOfData nodes are leaf nodes
		int[] clusterIndex = new int[numOfData];
		for(int i=0; i<numOfData; i++)	//initialize 
			clusterIndex[i] = numOfData + i - 1;
		
		//clustering
		for(int i=numOfData-1; i>0; i--)
		{
			int minRow=0, minColumn=1;
			double minDistance=distance[minRow][minColumn];
			//find the min distance
			for(int j=0; j<=i; j++) 
			{
				for(int k=j+1; k<=i; k++)
				{
					if(distance[j][k]<minDistance)		
					{
						minDistance = distance[j][k];
						minRow = j;
						minColumn = k;
					}
				}
			}
			
			//form the node
			node[i-1] = new Node();
			node[i-1].distance =  minDistance;
			node[i-1].child1 = clusterIndex[minRow];
			node[i-1].child2 = clusterIndex[minColumn];
			int countChild1 = (node[node[i-1].child1]!=null)?node[node[i-1].child1].count:1;
			int countChild2 = (node[node[i-1].child2]!=null)?node[node[i-1].child2].count:1;
			node[i-1].count = countChild1 + countChild2;
			
			//update distance matrix 
			//1: calculate the distance with the new cluster store at position of first point
			for(int j=0; j<=i; j++)
			{
				if(j!=minRow && j!=minColumn)
				{
					int countNodeJ = (node[j]!=null)?node[j].count:1;
					distance[minRow][j] = updateDistance(distance[minRow][minColumn], distance[minRow][j], distance[minColumn][j],
														countChild1, countChild2, countNodeJ);
					distance[j][minRow] = distance[minRow][j];
				}
			}			
			//2: move the data at i (the last data) to the position of second data
			for(int j=0; j<=i; j++)
			{
				if(j!=minColumn)
				{
					distance[minColumn][j] = distance[i][j];
					distance[j][minColumn] = distance[minColumn][j];
				}
			}			
			clusterIndex[minRow] = i-1;
			clusterIndex[minColumn] = clusterIndex[i];
		}
	}
	
	private double[][] convertSimilarityToDistance(double[][] distance)
	{
		return distance;	//how to convert???
	}
	
	private double[][] convertToWardDistance(double[][] distance)
	{
		int numOfData = distance.length;
		for(int i=0; i<numOfData; i++)	
		{
			for(int j=i+1; j<numOfData; j++)
			{
				distance[i][j] = distance[i][j]/2;
				distance[j][i] = distance[i][j];
			}
		}
		return distance;
	}

	private void storeDistance(double[][] distance)
	{
		int numOfData = distance.length;
		this.distances = new double[numOfData][numOfData];
		for(int i=0; i<numOfData; i++)	
		{
			System.arraycopy(distance[i], 0, this.distances[i], 0, numOfData);
		}
	}

	private double updateDistance(double distIJ, double distIS, double distJS, int countI, int countJ, int countS)
	{
		double distance = 0;
		switch(algorithm)
		{
		case ALOGRITHM_COMPLETE:
			distance = distIS>distJS ? distIS : distJS;
			break;
		case ALOGRITHM_WPGMA: 
			distance = (distIS + distJS)/2;
			break;
		case ALOGRITHM_UPGMA: 
			distance = (countI*distIS + countJ*distJS)/(countI+countJ);
			break;
		case ALOGRITHM_WPGMC: 
			distance = (distIS + distJS)/2 - distIJ/4;
			break;
		case ALOGRITHM_UPGMC: 
			distance = (countI*distIS + countJ*distJS - countI*countJ*distIJ/(countI+countJ))/(countI+countJ);
			break;
		case ALOGRITHM_WARD: 
			distance = ((countI+countS)*distIS + (countJ+countS)*distJS - countS*distIJ)/(countI+countJ+countS);
			break;		
		default:	//ALOGRITHM_SINGLE
			distance = distIS<distJS ? distIS : distJS;
		}
		return distance;
	}
	
	public int[] cluster(int num)
	{
		return cluster(num, false);
	}
	
	public int[] cluster(int num, boolean analysis)
	{
		if(numOfData<num)
			return null;
		
		scatterWithinClass = 0.0;
		scatterBetweenClass = 0.0;
		
		numOfCluster = num;
		clusters = new int[numOfCluster];
		if(numOfCluster==1)
			clusters[0] = 0;
		
		//clustering
		for(int i=0, count=0; i<numOfCluster-1; i++)
		{
			if(node[i].child1>=numOfCluster-1)
			{
				clusters[count++] = node[i].child1;
			}
			if(node[i].child2>=numOfCluster-1)
			{
				clusters[count++] = node[i].child2;
			}
		}
		
		//analysis the quality of the clustering
		if(analysis)
		{
			int[][] clusterData = new int[numOfCluster][];
			for(int i=0; i<numOfCluster; i++)
			{
				clusterData[i] = listClusterData(clusters[i]);			
			}
			
			//calculateScatterWithinClass();
			double[] distanceWithin = new double[numOfCluster];
			for(int i=0; i<numOfCluster; i++)
			{
				int dataCount = clusterData[i].length;
				for(int j=0; j<dataCount; j++)
				{
					for(int k=j+1; k<dataCount; k++)
					{
						distanceWithin[i] += distances[clusterData[i][j]][clusterData[i][k]];
					}
				}
				if(dataCount>1)
					scatterWithinClass += 2*distanceWithin[i] / (dataCount-1); // dataCount * (distanceWithin[i] / dataCount*(dataCount-1)/2);
			}			
			scatterWithinClass /= numOfData;
			
			//calculateScatterBetweenClass();	
			if(numOfCluster>1)
			{
				double[] distanceBetween = new double[numOfCluster*(numOfCluster-1)/2];
				int betweenCount = 0;
				for(int i=0; i<numOfCluster; i++)
				{
					int dataCountI = clusterData[i].length;
					for(int j=i+1; j<numOfCluster; j++)
					{
						int dataCountJ = clusterData[j].length;
						for(int iIndex=0; iIndex<dataCountI; iIndex++)
						{
							for(int jIndex=0; jIndex<dataCountJ; jIndex++)
							{
								distanceBetween[betweenCount] += distances[clusterData[i][iIndex]][clusterData[j][jIndex]];
							}
						}
						scatterBetweenClass += distanceBetween[betweenCount++] / (dataCountI*dataCountJ); 
					}
				}			
				scatterBetweenClass /= numOfCluster*(numOfCluster-1)/2;
			}
		}
		return clusters;
	}

	public int[] listClusterData(int nodeIndex)
	{
		int[] data = getDataByNode(nodeIndex);
		//recover original data index
		int length = data.length;
		for(int i=0; i<length; i++)
			data[i] -= (numOfData-1);
		
		Arrays.sort(data);
		return data;
	}
	
	public int[] getDataByNode(int nodeIndex)
	{
		if(node[nodeIndex]==null)
			return new int[]{nodeIndex};
		
		int[] data = new int[node[nodeIndex].count];
		int child = node[nodeIndex].child1;	
		int[] childData = getDataByNode(child);
		int lenghtOfChildData = childData.length;
		System.arraycopy(childData, 0, data, 0, lenghtOfChildData);
		child = node[nodeIndex].child2;	
		childData = getDataByNode(child);
		System.arraycopy(childData, 0, data, lenghtOfChildData, childData.length);
		
		return data;
	}
	
	public double getScatterBetweenClass() {
		return scatterBetweenClass;
	}

	public double getScatterWithinClass() {
		return scatterWithinClass;
	}

	public int getAlgorithm() {
		return algorithm;
	}


}
