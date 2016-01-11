package mlc.datamining.util.distance;

import weka.core.Instance;
import weka.core.ManhattanDistance;

public class JaccardDistance extends ManhattanDistance{

	@Override
	public String globalInfo() {
		return "Jaccard distance";
	}

	@Override
	protected double updateDistance(double arg0, double arg1) {
		return 0;
	}

	public String getRevision() {
		return "0.1";
	}

	@Override
	public double distance(Instance arg0, Instance arg1) {
		int sumSame = 0;
		int sumDiff = 0;
		for(int i=0; i<arg0.numAttributes(); i++) {
			int value0 = (int)arg0.value(i);
			int value1 = (int)arg1.value(i);
			if(value0!=value1) {
				sumDiff++;
			} else if(value0==1) {
				sumDiff++;
				sumSame++;
			}
		}
		double similarity = (double)sumSame / (double)sumDiff;
		return 1.0 - similarity;
	}
	
	

}
