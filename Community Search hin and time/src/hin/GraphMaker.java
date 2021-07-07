package hin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Config;
import util.DataReader;

public class GraphMaker {
	private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
	private int vertexType[] = null;//vertex -> type
	private int edgeType[] = null;//edge -> type
	
	
	public GraphMaker(int graph[][], int vertexType[], int edgeType[]) {
		this.graph = graph;
		this.vertexType = vertexType;
		this.edgeType = edgeType;
	}
	
	public Map<String, Double> makeWeightedGraph(MetaPath meta_path) {
		Map<String, Double> weightMap = new HashMap<String, Double>();
		for(int i = 0; i < vertexType.length; i++) {
			//center node
			int vertex_type = meta_path.vertex[meta_path.pathLen/2]; //center node type
			if(vertexType[i] != vertex_type) continue;
//			System.out.println("center node[" + i + "] vertex_type: " + vertex_type);
			
			//2-nd meta-path node
			vertex_type = meta_path.vertex[meta_path.pathLen/2 + 1];
			int edge_type = meta_path.edge[meta_path.pathLen/2];
			Set<Integer> node_set= new HashSet<Integer>();
			int nbArr[] = graph[i];
//			System.out.println("2-nd vertex_type: " + vertex_type + ", 2-nd edge_type: " + edge_type);
			//add 2-nd meta-path neighbors
			for(int j = 0;j < nbArr.length;j += 2) {
				int nbVertexID = nbArr[j], nbEdgeID = nbArr[j + 1];
//				System.out.println("vertexType[" + nbVertexID + "]: " + vertexType[nbVertexID] + ", edgeType[" + nbEdgeID + "]: " + edgeType[nbEdgeID]);
				if(vertexType[nbVertexID] == vertex_type && edgeType[nbEdgeID] == edge_type)
					node_set.add(nbVertexID);
			}
//			System.out.println("node_set: " + node_set);
			
			//the rest meta-path node
			for(int q = meta_path.pathLen/2 + 1; q < meta_path.pathLen; q++) {
				Set<Integer> node_set_current= new HashSet<Integer>();
				vertex_type = meta_path.vertex[q + 1];
				edge_type = meta_path.edge[q];
				for(int node: node_set) {
					int nbArr2[] = graph[node];
					for(int j = 0;j < nbArr2.length;j += 2) {
						int nbVertexID = nbArr2[j], nbEdgeID = nbArr2[j + 1];
						if(vertexType[nbVertexID] == vertex_type && edgeType[nbEdgeID] == edge_type)
							node_set_current.add(nbVertexID);
					}
				}
				node_set = node_set_current;
			}
			
			if(node_set.size() == 1) continue;
			int relation_count = (node_set.size()*(node_set.size() - 1))/2;
//			double weight = 1/(double)relation_count;
			double weight = 1;
//			System.out.println("weight: " + weight);
			for(int node1: node_set) {
				String edge_half = node1 + ",";
				for(int node2: node_set) {
					if(node1 == node2) continue;
					String edge = edge_half + node2;
					if(!weightMap.containsKey(edge)) weightMap.put(edge, weight);
					else weightMap.put(edge, weightMap.get(edge) + weight);
				}
			}
		}		
		return weightMap;
	}
	
	public Map<Integer, Set<Integer>> makeSimpleGraph(MetaPath meta_path, double weight_threshold) {
		Map<Integer, Set<Integer>> simple_graph = new HashMap<Integer, Set<Integer>>();
		Map<String, Double> weighted_graph = makeWeightedGraph(meta_path);
		
		for(Map.Entry<String, Double> entry : weighted_graph.entrySet()) {
			if(entry.getValue() > weight_threshold) {
				String s[] = entry.getKey().split(",");
				int source_node = Integer.parseInt(s[0]);
				int targrt_node = Integer.parseInt(s[1]);
				if(!simple_graph.containsKey(source_node))
					simple_graph.put(source_node, new HashSet<Integer>());
				simple_graph.get(source_node).add(targrt_node);
			}
		}
		
		return simple_graph;
	}
	
	public Map<Integer, Set<String>> readCommunitys(String file_name) {
		Map<Integer, Set<String>> community_map = new HashMap<Integer, Set<String>>();
		
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(file_name));
			String line = null;
			while((line = stdin.readLine()) != null){
				String s[] = line.split(",");
				int id = Integer.parseInt(s[0]);
				Set<String> communitys = new HashSet<String>();
				for(int i = 1; i < s.length; i++) {
					communitys.add(s[i]);
				}
				community_map.put(id, communitys);
			}
			stdin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return community_map;
	}
	
	public static void main(String[] args) {
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge);
		int graph[][] = dataReader.readGraph();
		int vertexType[] = dataReader.readVertexType();
		int edgeType[] = dataReader.readEdgeType();
		
		GraphMaker graph_maker = new GraphMaker(graph, vertexType, edgeType);
//		int vertex[] = {0, 1, 0}, edge[] = {3, 1};
		int vertex[] = {0, 1, 2, 1, 0}, edge[] = {3, 6, 4, 1};
		double threshold = 1.0;
		int k_core = 3;
		MetaPath meta_path = new MetaPath(vertex, edge);
		System.out.println(meta_path.pathLen + ": " + meta_path);
		
		//write weighted graph
		Map<String, Double> weighted_graph = graph_maker.makeWeightedGraph(meta_path);
		try {
			BufferedWriter graph_file = new BufferedWriter(new FileWriter(Config.outputPath + "\\weight_graph.txt"));
			double average_weight = 0.0;
			for(Map.Entry<String, Double> entry : weighted_graph.entrySet()) {
				graph_file.write(entry.getKey() + "," + entry.getValue()+"\n");
				average_weight += entry.getValue();
			}
			graph_file.write("Average weight: " + average_weight/weighted_graph.size());
			graph_file.flush();
			graph_file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//make simple graph
		Map<Integer, Set<Integer>> simple_graph = graph_maker.makeSimpleGraph(meta_path, threshold);
		int degree = 0;
		for(Map.Entry<Integer, Set<Integer>> entry : simple_graph.entrySet()) {
			degree += entry.getValue().size();
			System.out.print(entry.getKey() + "");
			for(int nb: entry.getValue())
				System.out.print("," + nb);
			System.out.println();
		}
		System.out.println("average degree: " + degree/simple_graph.size());
		
		//community search result
		Map<Integer, Set<String>> community_map = graph_maker.readCommunitys(Config.dblpCommunity);
		BasicSearch search = new BasicSearch(simple_graph);
		try {
			BufferedWriter query_result = new BufferedWriter(new FileWriter(Config.outputPath + "\\Results.txt"));
			double average_precision = 0.0;
			int results_count = 0;
			Set<Integer> result = search.queryMinDistance(8184, k_core);
			for(int query: simple_graph.keySet()) {
//				Set<Integer> results = search.query(query, k_core);
				Set<Integer> results = search.queryMinDistance(query, k_core);
				Set<String> communitys_groundtruth = community_map.get(query);
				if(results != null) {
					query_result.write(""+query);
					int correct_count = 0;
					for(int node:results) {
						query_result.write(","+node);
						Set<String> friends_community = community_map.get(node);
						for(String community:friends_community) {
							if(communitys_groundtruth.contains(community)) {
								correct_count++;
								break;
							}
						}
					}
					double precision = (double)correct_count/results.size();
					query_result.write("\nCorrect count: " + correct_count + ", total size: " + results.size() + ",precision: " + precision + "\n");
					average_precision += precision;
					results_count++;
				}	
			}
			query_result.write("Average precision: " + average_precision/results_count);
			query_result.flush();
			query_result.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
