package mlc.datamining.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * @author chen_lu
 *
 */
public class Itemset {
	
	public static final String NON_ITEM_STRING = ", -";
	
	private HashSet<String> items = new HashSet<String>();
	private int frequency = 0;
	private String id = "";
	
	public Itemset(String[] items) {
		this(Arrays.asList(items));
	}

	public Itemset(List<String> items) {
		this(new HashSet<String>(items));
	}

	public Itemset(HashSet<String> items) {
		super();
		this.items = items;
		this.id = constructId(items);
	}

	public static String constructId(List<String> items) {
		Collections.sort(items, new Comparator<String>() {
		    public int compare(String o1, String o2) {
		    	return o1.compareTo(o2);
		    }});
		
		StringBuilder id = new StringBuilder();
		for(String item : items) {
			id.append(item).append("-");
		}
		return id.toString();
	}
	
	public static String constructId(String[] items) {
		return constructId(Arrays.asList(items));
	}
	
	public static String constructId(HashSet<String> items) {
		ArrayList<String> values = new ArrayList<String>(items);
		return constructId(values);
	}
	
	public static int differ(Itemset set1, Itemset set2) {
		HashSet<String> set = new HashSet<String>(set1.getItems());
		set.addAll(set2.getItems());
		if(set1.size() >= set2.size()) {
			return set.size() - set2.size();
		} else {
			return set.size() - set1.size();
		}
	}
	
	public static Itemset merge(Itemset set1, Itemset set2) {
		HashSet<String> set = new HashSet<String>(set1.getItems());
		set.addAll(set2.getItems());
		return new Itemset(set);
	}
	
	public static Itemset substract(Itemset set1, Itemset set2) {
		HashSet<String> set = new HashSet<String>(set1.getItems());
		set.removeAll(set2.getItems());
		return new Itemset(set);
	}
	
	public HashSet<String> getItems() {
		return items;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getId() {
		return id;
	}

	public void increaseFrequency() {
		this.frequency++;
	}

	public void resetFrequency() {
		this.frequency = 0;
	}

	public int size() {
		return this.items.size();
	}	
	
	public boolean contains(String item) {
		return this.items.contains(item);
	}	
	
	public boolean contains(Itemset item) {
		return Itemset.merge(this, item).size() == this.size();
	}	
	
	public static double distance(Itemset set1, Itemset set2) {
		return 1 - similarity(set1, set2);
	}
	
	public static double similarity(Itemset set1, Itemset set2) {
		int size1 = set1.size();
		int size2 = set2.size();
		int sizeTotal = size1 + size2;
		
		Itemset setMerge = Itemset.merge(set1, set2);
		int sizeMerge = setMerge.size();
		int sizeShared = sizeTotal - sizeMerge;

		return (sizeShared * 2.0)/ sizeTotal;
	}

	public static String constructId(String string) {
		return string+"-";
	}

}
