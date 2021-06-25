package dblp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Term {
	private static Map<String, Term> termMap = new HashMap<String, Term>();
	private static HashSet<String> stoplist;
	private static boolean stoplistLoaded = false;
	
	private int vertex_id;
	private String term;
	private HashSet<String> publication_list;
	private Map<String, Integer> edgeMap;
	
	private Term(String term, int vertex_id) {
        this.term = term;
        this.vertex_id = vertex_id;
        this.publication_list = new HashSet<String>();
        this.edgeMap = new HashMap<String, Integer>();
        termMap.put(term, this);
    }
	
	static public Term create(String term) {
		if(!stoplistLoaded)
			read_stoplist(Config.stopFile);
		if(stoplist.contains(term))
			return null;
		Term t;
        t = searchTerm(term);
        if (t == null) {
            t = new Term(term, Processor.getNewVertexId());
        }
        return t;
    }
	
	static public Term searchTerm(String term) {
        return termMap.get(term);
    }
	
	public int getVertexId() {
    	return vertex_id;
    }
	
	public void addPublication(String publication) {
		publication_list.add(publication);
    	int edge_id = Processor.getNewEdgeId();
    	edgeMap.put(publication, edge_id);
    	Processor.setEdgeType(edge_id, Config.T2P);
	}
	
	public String toString() {
        return "ID: " + vertex_id + ", Term: " + term + ", publication: " + publication_list;
    }
    
    /*
     * output: term_id paper1_id edge1_id paper2_id edge2_id ... 
     */
    public String getGraphLine() {
    	String graph = "" + vertex_id;
    	for(String publication: publication_list) {
    		graph += " " + Publication.searchPublication(publication).getVertexId() + " " + edgeMap.get(publication);
        }
        return graph;
    }
    
    static public Collection<Term> getAllTerms() {
        return termMap.values();
    }
    
	private static void read_stoplist(String file_name) {
		stoplist = new HashSet<String>();
		try {
			BufferedReader stdin = new BufferedReader(new FileReader(file_name));
			String line = null;
			while((line = stdin.readLine()) != null){
				stoplist.add(line);
			}
			stdin.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
