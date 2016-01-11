package mlc.datamining.util.distance;


public class TFIDF 
{
	private int numOfData = 0, numOfFeature = 0;
	private int[][] data = null; 	//input format: mxn matrix of count
	private double[][] tfidf = null; 	
	private double[][] similarity = null; 	

	public TFIDF(int[][] d) 
	{
		this.data = d;
		this.numOfData = data.length;
		this.numOfFeature = data[0].length;	
		calculateTFIDF();
		calculateSimilarity();
	}

	private double[][] tf()	
	{
		double[][] tfMatrix = new double[numOfData][numOfFeature];
		for(int i=0; i<numOfData; i++)
		{
			for(int j=0; j<numOfFeature; j++)
			{
				int value = data[i][j];
				if(value==1 || value==0)
					tfMatrix[i][j] = value;
				else
					tfMatrix[i][j] = 1 + Math.log10(1+Math.log10(value));
			}
		}
		return tfMatrix;
	}
	
	private double[] idf()	
	{
		double[] idfVector = new double[numOfFeature];
		for(int i=0; i<numOfFeature; i++)
		{
			int countDataHavingFeature = 0;
			for(int j=0; j<numOfData; j++)
			{
				if(data[j][i]>0)
					countDataHavingFeature++;
			}
			if(countDataHavingFeature>0)
				idfVector[i] = Math.log10((1.0+numOfData)/countDataHavingFeature);
		}
		
		return idfVector;	
	}
		
	private void calculateTFIDF()	
	{
		double[][] tf = tf();
		double[] idf = idf();	
		tfidf = new double[numOfData][numOfFeature];
		for(int i=0; i<numOfData; i++)
		{
			for(int j=0; j<numOfFeature; j++)
			{
				tfidf[i][j] = tf[i][j]*idf[j];
			}
		}
	}
	
	private void calculateSimilarity()	
	{
		similarity = new double[numOfData][numOfData];
		double[] norm = new double[numOfData];
		for(int i=0; i<numOfData; i++)
		{
			for(int j=0; j<numOfFeature; j++)
			{
				norm[i] += tfidf[i][j]*tfidf[i][j];
			}		
			norm[i] = Math.sqrt(norm[i]);
		}	
		
		for(int i=0; i<numOfData; i++)
		{
			similarity[i][i]=1;
			for(int j=i+1; j<numOfData; j++)
			{
				for(int k=0; k<numOfFeature; k++)
				{
					similarity[i][j] += tfidf[i][k]*tfidf[j][k];
				}
				similarity[i][j] /= norm[i]*norm[j];
				similarity[j][i] = similarity[i][j];
			}
		}
		
	}
	
	public double[][] getTfidf() {
		return tfidf;
	}

	public double[][] getSimilarity() {
		return similarity;
	}


	public static void main(String[] args) 
	{
		int[][] mn = new int[][]{{0,4,10,8,0,5,0},{5,19,7,16,0,0,32},{15,0,0,4,9,0,17},{22,3,12,0,5,15,0},{0,7,0,9,2,4,12}};
		TFIDF t = new TFIDF(mn);
		double[][] value = t.getTfidf();
		double[][] sim = t.getSimilarity();
		
		System.out.print("");

	}

}
