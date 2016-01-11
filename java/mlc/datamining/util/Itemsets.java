package mlc.datamining.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * @author chen_lu
 *
 */
public class Itemsets {
	
	private Hashtable<String, Itemset> allItemset = new Hashtable<String, Itemset>();
	private ArrayList<Itemset> allItemsetArray = new  ArrayList<Itemset>();
	boolean sync = false;
	
	public Itemset add(String itemStr) {
		StringTokenizer st = new StringTokenizer(itemStr, Itemset.NON_ITEM_STRING);
		ArrayList<String> items = new ArrayList<String>();
		while(st.hasMoreTokens()) {
			items.add(st.nextToken());
		}
		return add(items);
	}
	
	public Itemset add(String[] items) {
		HashSet<String> set = new HashSet<String>();
		for(String item : items) {
			set.add(item);
		}
		return add(set);
	}
	
	public Itemset add(ArrayList<String> items) {
		return add(new HashSet<String>(items));
	}
	
	public Itemset add(HashSet<String> items) {
		String id = Itemset.constructId(items);
		Itemset itemset = allItemset.get(id);
		if(itemset==null) {
			itemset = new Itemset(items);
			allItemset.put(id, itemset);
			sync = false;
		}
		itemset.increaseFrequency();
		return itemset;
	}

	public boolean remove(String id) {
		Itemset itemset = allItemset.get(id);
		if(itemset==null) {
			return false;
		} else {
			allItemset.remove(id);
			sync = false;
			return true;
		}
	}

	public Itemset get(String id) {
		return allItemset.get(id);
	}

	public Itemset get(int index) {
		if(sync==false) {
			syncArray();
		}
		return allItemsetArray.get(index);
	}

	public boolean contain(String id) {
		return get(id)!=null;
	}

	private void syncArray() {
		//sort by id
		allItemsetArray = new ArrayList(allItemset.values());
		Collections.sort(allItemsetArray, new Comparator<Itemset>() {
		    public int compare(Itemset o1, Itemset o2) {
		    	return o2.getId().compareTo(o1.getId());
		    }});

		sync = true;
	}

	public ArrayList<Itemset> sortByFrequency() {
		ArrayList itemsetArray = new ArrayList(allItemset.values());
		Collections.sort(itemsetArray, new Comparator<Itemset>() {
		    public int compare(Itemset o1, Itemset o2) {
		    	return o2.getFrequency() - o1.getFrequency();
		    }});

		return itemsetArray;
	}

	public ArrayList<Itemset> sortByLength() {
		ArrayList itemsetArray = new ArrayList(allItemset.values());
		Collections.sort(itemsetArray, new Comparator<Itemset>() {
		    public int compare(Itemset o1, Itemset o2) {
		    	return o2.getItems().size() - o1.getItems().size();
		    }});

		return itemsetArray;
	}

	public ArrayList<Itemset> sortById() {
		if(sync==false) {
			syncArray();
		}
		return allItemsetArray;		
	}

	public int size() {
		if(sync==false) {
			syncArray();
		}
		return allItemsetArray.size();		
	}
	
	public void resetFrequency() {
		int size = allItemset.size();
		for(int i=0; i<size; i++) {
			Itemset set = get(i);
			set.resetFrequency();
			allItemset.put(set.getId(), set);
		}
	}

}
