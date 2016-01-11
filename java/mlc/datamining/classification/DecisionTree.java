package mlc.datamining.classification;

import java.io.PrintStream;
import java.util.ArrayList;

public class DecisionTree 
{
	private static final double LOG_BASE2_FACTOR = Math.log(2.0);
	private static final double ZERO_THRESHOLD = 0.000001;
	private static final int MAX_TREE = 200;	//max number of decision nodes
	
	public class Node	//the hierarchical tree is implemented by a array
	{
		public int[] data = null;	//data index
		public double information;
		public int featureIndex = -1;
		public int childTrue = -1;
		public int childFalse = -1;
		
		public Node(int[] dataIndexes) 
		{
			this.data = dataIndexes;
		}
	}
	public Node[] node = null;	//the last node is the root
	private int numOfNode = 0;

	public class TrainingData
	{
		public int group;	//class id, must be >0
		public boolean[] features;
		
		public TrainingData(int g, boolean[] f) 
		{
			this.group = g;
			this.features = f;
		}
	}	
	TrainingData[] data = null;	//matrix of n by m
	int numOfGroup = 1;
	int numOfFeature = 0;
	
	public DecisionTree() 
	{
		this(MAX_TREE);
	}
	
	public DecisionTree(int size) 
	{
		this.node = new Node[size];
	}
	
	public void build(TrainingData[] d) 
	{
		this.data = d;
		retrieveDataInfo();
		
		int[] index = new int[data.length];
		for(int i=0; i<data.length; i++)
			index[i] = i;
		
		createNode(index, numOfNode);
	}
	
	private void retrieveDataInfo()
	{
		this.numOfFeature = data[0].features.length;
		for(int i=0; i<data.length; i++)
			if(data[i].group>numOfGroup)
				numOfGroup = data[i].group;
		numOfGroup++;
	}
	
	private void createNode(int[] dataIndex, int nodeIndex)
	{
		if(nodeIndex>=node.length)
			return;
		
		node[nodeIndex] = new Node(dataIndex);
		node[nodeIndex].information = calculateInformation(dataIndex);
		if(node[nodeIndex].information<ZERO_THRESHOLD)
			return;
	
		int[] childTrueIndex=null, childFalseIndex=null;
		double information = 1000;	//a big number
		
		//calculate information for each feature
		for(int i=0; i<numOfFeature; i++)	
		{
			double informationTrue = 0, informationFalse = 0;
			ArrayList<Integer> indexTrue = new ArrayList<Integer>();
			ArrayList<Integer> indexFalse = new ArrayList<Integer>();
			for(int j=0; j<dataIndex.length; j++)
			{
				if(data[dataIndex[j]].features[i])
					indexTrue.add(dataIndex[j]);
				else
					indexFalse.add(dataIndex[j]);				
			}
			int sizeTrue = indexTrue.size();
			int sizeFalse = indexFalse.size();
			if(sizeTrue!=0 && sizeFalse!=0)	//this feature has different values
			{
				Integer[] integerTrue = new Integer[sizeTrue];
				int[] intTrue = convertIntegerArray(indexTrue.toArray(integerTrue));
				informationTrue = calculateInformation(intTrue);
				Integer[] integerFalse = new Integer[sizeFalse];
				int[] intFalse = convertIntegerArray(indexFalse.toArray(integerFalse));
				informationFalse = calculateInformation(intFalse);
				
				double featureInformation = (sizeTrue*informationTrue + sizeFalse*informationFalse)/(sizeTrue + sizeFalse);
				if(information>featureInformation)
				{
					information=featureInformation;
					node[nodeIndex].featureIndex = i;
					childTrueIndex = intTrue;
					childFalseIndex = intFalse;
				}
			}
		}
		
		//recusively create child node
		node[nodeIndex].childTrue = ++numOfNode;
		createNode(childTrueIndex, numOfNode);
		node[nodeIndex].childFalse = ++numOfNode;
		createNode(childFalseIndex, numOfNode);
		
	}
	
	private int[] convertIntegerArray(Integer[] index)
	{
		int[] indexInt = new int[index.length]; 
		for(int i=0; i<index.length; i++)
			indexInt[i] = index[i].intValue();
		
		return indexInt;
	}
	
	private double calculateInformation(int[] index)
	{
		int[] count = new int[numOfGroup];
		int totalCount = 0;
		for(int i=0; i<index.length; i++, totalCount++)
			count[data[index[i]].group]++;
		
		double information = 0;
		for(int i=0; i<numOfGroup; i++)
		{
			if(count[i]>0)
			{
				double probability = (double)count[i]/totalCount;
				information += probability * Math.log(probability);
			}
		}
		
		return -information/LOG_BASE2_FACTOR;
	}

	public void printTree(PrintStream out)
	{
		printNode(out, 0, 0);
	}
	
	private void printNode(PrintStream out, int index, int level)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<level; i++)
			sb.append("|      ");
		String indent = sb.toString();
			
		if(node[index].featureIndex!=-1)
		{
			out.print(node[index].featureIndex + "--1--");
			printNode(out, node[index].childTrue, level+1);
			out.println();
			out.println(indent + "|");
			out.print(indent + "0-----");
			printNode(out, node[index].childFalse, level+1);			
		}
		else
			out.print("[" + data[node[index].data[0]].group + "]");
				
		return;
	}

	public static void main(String[] args) 
	{
		TrainingData[] data = new TrainingData[14];
		DecisionTree dt = new DecisionTree();
		data[0] = dt.new TrainingData(0, new boolean[]{false,true,false,false});
		data[1] = dt.new TrainingData(0, new boolean[]{false,true,false,false});
		data[2] = dt.new TrainingData(1, new boolean[]{true,false,true,false});
		data[3] = dt.new TrainingData(1, new boolean[]{true,false,false,true});
		data[4] = dt.new TrainingData(1, new boolean[]{false,false,true,false});
		data[5] = dt.new TrainingData(0, new boolean[]{false,true,false,false});
		data[6] = dt.new TrainingData(1, new boolean[]{true,true,false,true});
		data[7] = dt.new TrainingData(0, new boolean[]{false,true,false,true});
		data[8] = dt.new TrainingData(1, new boolean[]{true,true,false,false});
		data[9] = dt.new TrainingData(1, new boolean[]{false,false,true,true});
		data[10] = dt.new TrainingData(1, new boolean[]{true,false,false,false});
		data[11] = dt.new TrainingData(1, new boolean[]{true,true,false,true});
		data[12] = dt.new TrainingData(1, new boolean[]{false,true,true,true});
		data[13] = dt.new TrainingData(0, new boolean[]{false,false,false,false});
		
		dt.build(data);
		dt.printTree(System.out);
	}

	
	
}
