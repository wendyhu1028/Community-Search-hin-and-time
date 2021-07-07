package hin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BasicSearch {
	private Map<Integer, Set<Integer>> graph_map;
	private Map<Integer, Set<Integer>> kcore_map;
	private int current_k = 0;
	
	public BasicSearch(Map<Integer, Set<Integer>> graph_map) {
		this.graph_map = graph_map;
	}
	
	public Set<Integer> query(int query_id, int k) {
		//step 1: if k changed, find k-core
		if(current_k != k)
			findKCore(k);
		
		//step 2: find the connected component containing query node
		if(!kcore_map.containsKey(query_id)) return null;
		Set<Integer> result = new HashSet<Integer>();//nodes which have been put into queue
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(query_id);
		result.add(query_id);
		while(queue.size() > 0) {
			int node = queue.poll();
			for(int nb:kcore_map.get(node)) {//enumerate all the neighbor
				if(!result.contains(nb)) {
					queue.add(nb);
					result.add(nb);
				}
			}
		}
		result.remove(query_id);
		return result;
	}
	
	public Set<Integer> queryMinDistance(int query_id, int k) {
		//step 1: if k changed, find k-core
		if(current_k != k)
			findKCore(k);
		
		//step 2: find the connected component containing query node
		if(!kcore_map.containsKey(query_id)) return null;
		Set<Integer> result = new HashSet<Integer>();//nodes which have been put into queue
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(query_id);
		result.add(query_id);
		Map<Integer, Integer> distance_map = new HashMap<Integer, Integer>();
		distance_map.put(query_id, 0);
		ArrayList<Set<Integer>> nodes_distance = new ArrayList<Set<Integer>>();
		Set<Integer> nodes = new HashSet<Integer>();
		nodes.add(query_id);
		nodes_distance.add(nodes);
		Map<Integer, Set<Integer>> retain_map = new HashMap<Integer, Set<Integer>>();
		while(queue.size() > 0) {
			int node = queue.poll();
			int distance = distance_map.get(node) + 1;
			retain_map.put(node, kcore_map.get(node));
			for(int nb:kcore_map.get(node)) {//enumerate all the neighbor
				if(!result.contains(nb)) {
					queue.add(nb);
					result.add(nb);
					distance_map.put(nb, distance);
					if(nodes_distance.size() < (distance+1))
						nodes_distance.add(new HashSet<Integer>());
					nodes_distance.get(distance).add(nb);
				}
			}
		}
		
		//step 3: delete node from the longest distance
		Queue<Integer> queue2 = new LinkedList<Integer>();
		Set<Integer> delete_set = new HashSet<Integer>();
		for(int i = nodes_distance.size()-1; i>0; i--) {
			queue2.addAll(nodes_distance.get(i));
			delete_set.addAll(nodes_distance.get(i));
			
			//delete nodes less than k neighbors
			while(queue2.size() > 0) {
				int node = queue2.poll();//delete current node
				if(!retain_map.containsKey(node)) continue;
				Set<Integer> nb_set = retain_map.get(node);
				for(int nb:nb_set) {//update neighbor nodes
					if(!delete_set.contains(nb)) {
						retain_map.get(nb).remove(node);
						if(retain_map.get(nb).size() < k) {
							queue2.add(nb);
							delete_set.add(nb);
						}
					}
				}
				retain_map.remove(node);//remove the node
			}
			if(retain_map.size() < k) {
				result.remove(query_id);
				return result;
			}
			result.removeAll(delete_set);
		}
		kcore_map.putAll(kcore_map); 
		
		result.remove(query_id);
		return result;
	}
	
	public void findKCore(int k) {
		kcore_map = new HashMap<Integer, Set<Integer>>();
		kcore_map.putAll(graph_map); 
		Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue
		
		//step 1: find the node can be deleted in the first round
		Set<Integer> delete_set = new HashSet<Integer>();
		for(Map.Entry<Integer, Set<Integer>> entry : kcore_map.entrySet()) {
			int node = entry.getKey();
			if(entry.getValue().size() < k) {
				queue.add(node);
				delete_set.add(node);
			}
		}
		
		//step 2: delete vertices whose degrees are less than k
		while(queue.size() > 0) {
			int node = queue.poll();//delete current node
			Set<Integer> nb_set = kcore_map.get(node);
			for(int nb:nb_set) {//update neighbor nodes
				if(!delete_set.contains(nb)) {
					kcore_map.get(nb).remove(node);
					if(kcore_map.get(nb).size() < k) {
						queue.add(nb);
						delete_set.add(nb);
					}
				}
			}
			kcore_map.remove(node);//remove the node
		}
		
		current_k = k;
	}

}
