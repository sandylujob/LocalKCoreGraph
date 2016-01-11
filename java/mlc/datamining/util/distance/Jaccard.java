package mlc.datamining.util.distance;

import java.util.Date;

public class Jaccard {
	public static double[][] similarity(int[][] data) {// input format: mxn matrix of 1/0
		if (data == null || data.length == 0)
			return null;

		int numOfData = data.length;
		int numOfFeature = data[0].length;
		double[][] similarity = new double[numOfData][numOfData];
		for (int i = 0; i < numOfData; i++) {
			if (i % 100 == 0) {
				System.out.println(i + ": " + new Date());
			}
			for (int j = i + 1; j < numOfData; j++) {
				int countAnd = 0, countOr = 0;
				for (int k = 0; k < numOfFeature; k++) {// calculate the distance
					if ((data[i][k] != data[j][k])) {
						countOr++;
					} else if (data[i][k] == 1) {
						countAnd++;
						countOr++;
					}
				}
				similarity[i][j] = (double) countAnd / countOr;
				similarity[j][i] = similarity[i][j];
			}
		}

		return similarity;
	}

	public static double[][] similarity(int[][] data, double[] weights) {
		if (data == null || data.length == 0)
			return null;

		int numOfData = data.length;
		int numOfFeature = data[0].length;
		double[][] similarity = new double[numOfData][numOfData];
		for (int i = 0; i < numOfData; i++) {
			if (i % 100 == 0) {
				System.out.println(i + ": " + new Date());
			}
			for (int j = i + 1; j < numOfData; j++) {
				double countAnd = 0, countOr = 0;
				for (int k = 0; k < numOfFeature; k++) {// calculate the distance
					if ((data[i][k] != data[j][k])) {
						countOr += weights[k];
					} else if (data[i][k] == 1) {
						countAnd += weights[k];
						countOr += weights[k];
					}
				}
				similarity[i][j] = (double) countAnd / countOr;
				similarity[j][i] = similarity[i][j];
			}
		}

		return similarity;
	}

}
