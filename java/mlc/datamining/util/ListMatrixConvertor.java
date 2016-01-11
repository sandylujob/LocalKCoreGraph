package mlc.datamining.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 * @author chen_lu
 *
 */
public class ListMatrixConvertor {

	public static final int DATA_FORMAT_LIST = 0;
	public static final int DATA_FORMAT_MATRIX = 1;
	
	private String seperator = ",: ";
	private int[][] matrix;
	private String[] list;
	private ArrayList<String> items;
	private int numOfRecord, numOfItem;
	boolean header = false;
	private String[] headerIndex;

	public ListMatrixConvertor() {
		this(false);
	}

	public ListMatrixConvertor(boolean header) {
		super();
		this.header = header;
	}

	public int[][] convertListToMatrix(String[] inputList) {
		Hashtable<String, Integer> featureIndex = new Hashtable<String, Integer> ();
		return convertListToMatrix(inputList, featureIndex);
	}
	
	public int[][] convertListToMatrix(ArrayList<String> inputList, Hashtable<String, Integer> featureIndex) {
		int count = inputList.size();
		String[] inputArr = new String[count];
		for(int i=0; i<count; i++) {
			inputArr[i] = inputList.get(i);
		}
		return convertListToMatrix(inputArr, featureIndex);
	}
	
	public int[][] convertListToMatrix(String[] inputList, Hashtable<String, Integer> featureIndex) {

		this.list = inputList;
		this.numOfRecord = list.length;
		if(header) {
			headerIndex = new String[numOfRecord];
		}
		HashSet<String> itemset = new HashSet<String>();
		for(int i=0; i<numOfRecord; i++) {
			StringTokenizer st = new StringTokenizer(list[i], seperator);
			if(header) {
				if(st.hasMoreTokens())
				headerIndex[i] = st.nextToken();
			}
			while(st.hasMoreTokens()) {
				itemset.add(st.nextToken());
			}
		}
		
		items = new ArrayList<String>(itemset);
		Collections.sort(items);
		this.numOfItem = items.size();
		for(int i=0; i<numOfItem; i++) {
			featureIndex.put(items.get(i), i);
		}
		
		matrix = new int[numOfRecord][numOfItem];
		for(int i=0; i<numOfRecord; i++) {
			StringTokenizer st = new StringTokenizer(list[i], seperator);
			if(header) {
				if(st.hasMoreTokens())
				st.nextToken();	//skip first token
			}
			while(st.hasMoreTokens()) {
				String itemName = st.nextToken();
				int itemIndex = featureIndex.get(itemName).intValue();
				matrix[i][itemIndex] = 1;
			}
		}
		return matrix;
	}
	
	public int[][] getMatrix() {
		return matrix;
	}

	public String[] getList() {
		return list;
	}

	public int getNumOfRecord() {
		return numOfRecord;
	}

	public int getNumOfItem() {
		return numOfItem;
	}


	public static void main(String[] args) throws SQLException {

		System.out.println(new Date());  
		ListMatrixConvertor convertor = new ListMatrixConvertor(true);
		String[] list = new String[] {"1,2,3", "2,3,4,5,6", "1,5", "2,5,6"};
		int[][] m = convertor.convertListToMatrix(list);
		System.out.println(m);  
		System.out.println(new Date());  

	}


}
