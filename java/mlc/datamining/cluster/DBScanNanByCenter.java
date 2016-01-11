package mlc.datamining.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author chen_l
 *
 */
public class DBScanNanByCenter {

	public int numOfCluster = 0;
	public int[] dataClusterId = null;
	double[] data = null;	//distance of data to the center
	ArrayList<IndexData> indexData = new ArrayList<IndexData>();;
	
	class IndexData {
		int index;
		double value;
		public IndexData(int i, double d) {
			this.index = i;
			this.value = d;
		}
	}
	
	public DBScanNanByCenter(double[] data) {
		this.data = data;
		for(int i=0; i<data.length; i++) {
			IndexData iData = new IndexData(i, data[i]);
			indexData.add(iData);
		}
		this.dataClusterId = new int[data.length];
		cluster();
	}

	private void cluster() {
		Collections.sort(indexData, new Comparator<IndexData>() {
		    public int compare(IndexData o1, IndexData o2) {
		    	return o1.value>o2.value ? 1 : -1;
		    }
		});

		for(int i=0; i<indexData.size(); i++) {
			IndexData idata = indexData.get(i);
			dataClusterId[idata.index] = numOfCluster;
			double radius = idata.value;
			double currentPosition = idata.value;
			
			for(int j=i+1; j<data.length; j++) {
				IndexData jdata = indexData.get(j);
				if(jdata.value >= (currentPosition+radius)) {
					i = j-1;
					break;
				} else {
					dataClusterId[jdata.index] = numOfCluster;
					currentPosition = jdata.value;
					i = j;
				}
			}
			numOfCluster++;
		}
	}
	
	public static void main(String[] args) throws IOException 
	{
		double[] testData = new double[] {0.5, 0.13, 0.8, 0.9, 0.25, 0.1}; 
		DBScanNanByCenter dbCluster = new DBScanNanByCenter(testData);
		System.out.println(dbCluster.numOfCluster);
		System.out.println(dbCluster.dataClusterId);
		
	}
}
