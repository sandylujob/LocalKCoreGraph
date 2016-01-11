package mlc.datamining.util.distance;

public class EuclideanDistance {
	
	double[][] data = null;
	double[][] distance = null;
	private int numOfData = 0;
	private int numOfFeature = 0;

	public EuclideanDistance(double[][] data) {
		this.data = data;
		this.numOfData = data.length;
		this.numOfFeature = data[0].length;
		build();
	}

	private void build() {
		distance = new double[numOfData][numOfData];
		for(int i=0; i<numOfData; i++){
			for(int j=i+1; j<numOfData; j++) {
				distance[i][j] = calculateDistance(data[i], data[j]);
				distance[j][i] = distance[i][j];
			}
		}
	}

	private double calculateDistance(double[] data1, double[] data2) {
		double sum = 0;
		for(int i=0; i<numOfFeature; i++) {
			sum += (data1[i]-data2[i]) * (data1[i]-data2[i]);
		}
		return Math.sqrt(sum);
	}

	public double[][] getDistance() 
	{
		return distance;
	}

}
