package mlc.datamining.util;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author chen_lu
 *
 */
public class ItemsetGraphNode {

	private String id = null;
	private HashSet<String> parentIds = new HashSet<String>();
	private HashSet<String> childIds = new HashSet<String>();

	public ItemsetGraphNode() {
		super();
	}

	public ItemsetGraphNode(Itemset item) {
		super();
		this.id = item.getId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setId(int i) {
		this.id = Integer.toString(i);
	}

	public void addParent(String parent) {
		parentIds.add(parent);
	}
	
	public void addParent(int j) {
		addParent(Integer.toString(j));
	}

	public ArrayList<String> getParents() {
		return new ArrayList<String>(parentIds);
	}
	
	public int parentSize() {
		return parentIds.size();
	}

	public void addChild(String child) {
		childIds.add(child);
	}
	
	public void addChild(int j) {
		addChild(Integer.toString(j));
	}

	public ArrayList<String> getChildren() {
		return new ArrayList<String>(childIds);
	}
	
	public int childSize() {
		return childIds.size();
	}

}
