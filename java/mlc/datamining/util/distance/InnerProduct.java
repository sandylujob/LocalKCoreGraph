package mlc.datamining.util.distance;

/**
 * @author chen_lu
 *
 */
public class InnerProduct {
	private double[] weights = null;
	private int[][] data = null;
	private double[] normValue = null;
	double[][] similarity = null;
	private int numOfData = 0;
	private int numOfFeature = 0;
	
	public InnerProduct(int[][] data, double[] weights, boolean localWeight) {
		super();
		if (data == null || data.length == 0)
			return;
		
		this.data = data;		
		numOfData = data.length;
		numOfFeature = data[0].length;
		if(localWeight) {
			getWeights();
		} else {
			this.weights = weights;
		}

		calculateNormValue();
		calculateSimilarity();
	}

	public InnerProduct(int[][] data) {
		this(data, null);
	}

	public InnerProduct(int[][] data, double[] weights) {
		this(data, null, false);
	}

	public double getSimilarity(int index1, int index2) {
		return similarity[index1][index2];
	}

	public double[][] getSimilarity() {
		return similarity;
	}

	private void calculateNormValue() {
		normValue = new double[numOfData];
		for(int i=0; i<numOfData; i++) {
			normValue[i] = 0.0;
			for(int j=0; j<numOfFeature; j++) {
				if(data[i][j]==1) {
					if(weights!=null) {
						normValue[i] += weights[j]*weights[j];
					} else {
						normValue[i] += 1.0;
					}
				}
			}
			normValue[i] = Math.sqrt(normValue[i]);
		}
	}

	private void calculateSimilarity() {
		similarity = new double[numOfData][numOfData];
		for (int i=0; i<numOfData; i++) {
			similarity[i][i] = 1.0;
			for (int j=i+1; j <numOfData; j++) {
				for (int k = 0; k < numOfFeature; k++) {
					if ((data[i][k]==1 && data[j][k]==1)) {
						if(weights!=null) {
							similarity[i][j] += weights[k]*weights[k];
						} else {
							similarity[i][j] += 1.0;
						}
					}
				}
				similarity[i][j] = similarity[i][j]/(normValue[i]*normValue[j]);
				similarity[j][i] = similarity[i][j];
			}
		}
	}

	public double[] getWeights() {
		weights = new double[numOfFeature];
		for(int i=0; i<numOfFeature; i++) {
			weights[i] = 0.1 / numOfData;	//assume it is zero to avoid log(0)
			for(int j=0; j<numOfData; j++) {
				if(data[j][i]!=0) {
					weights[i] += 1.0;
				}
			}
			weights[i] = Math.log(numOfData/weights[i]);
		}
		
		return weights;
	}

}
