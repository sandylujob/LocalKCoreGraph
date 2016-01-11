package mlc.datamining.util;

import java.util.HashSet;

/**
 * @author chen_lu
 *
 */
public class NeighborNode {

	private String id = null;
	private HashSet<String> neighborIds = new HashSet<String>();


	public NeighborNode(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void addNeighbor(String neighbor) {
		neighborIds.add(neighbor);
	}
	
	public HashSet<String> getNeighbors() {
		return neighborIds;
	}
	
	public int neighborSize() {
		return neighborIds.size();
	}

	public void setNeighbor(HashSet<String> neighbors) {
		this.neighborIds = neighbors;
	}

	public boolean hasNeightbor(String id) {
		return neighborIds.contains(id);
	}
}
