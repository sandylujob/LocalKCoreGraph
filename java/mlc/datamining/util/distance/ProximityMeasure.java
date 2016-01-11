package mlc.datamining.util.distance;


public class ProximityMeasure 
{
	public static final int METHOD_JACCARD = 0;
	public static final int METHOD_TFIDF = 1;
	public static final int METHOD_JACCARD_WEIGHTED = 2;
	public static final int METHOD_INNER_PRODUCT = 3;
	public static final int METHOD_INNER_PRODUCT_LOCAL_WEIGHT = 4;
	public static final int METHOD_EUCLIDEAN = 5;
	
	private int method = METHOD_JACCARD;
	private double[] weights = null;
	
	public ProximityMeasure(int m) 
	{
		this.method = m;		
	}
	
	public double[][] getDistance(double[][] data)
	{
		switch(method)
		{
			case METHOD_EUCLIDEAN:
				EuclideanDistance euclidean = new EuclideanDistance(data);
				return euclidean.getDistance();
			default:
		}
		return null;
	}
	
	public double[][] getSimilarity(int[][] data)
	{
		switch(method)
		{
			case METHOD_JACCARD:
				return Jaccard.similarity(data);
			case METHOD_JACCARD_WEIGHTED:
				return Jaccard.similarity(data, weights);
			case METHOD_INNER_PRODUCT:
				InnerProduct innnerProduct = new InnerProduct(data,weights);
				return innnerProduct.getSimilarity();
			case METHOD_INNER_PRODUCT_LOCAL_WEIGHT:
				InnerProduct innnerProductLocal = new InnerProduct(data, null, true);
				return innnerProductLocal.getSimilarity();
			case METHOD_TFIDF:
				TFIDF t = new TFIDF(data);
				return t.getSimilarity();
			default:
		}
		return null;
	}
	
	public double[][] getDistance(int[][] data) 
	{
		double[][] similarity = getSimilarity(data);
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

	/**
	 * @return the weights
	 */
	public double[] getWeights() {
		return weights;
	}

	/**
	 * @param weights the weights to set
	 */
	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	
}
