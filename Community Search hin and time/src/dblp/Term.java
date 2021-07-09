package dblp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import util.Config;

public class Term {
	private static Map<String, Term> termMap = new HashMap<String, Term>();
	private static HashSet<String> stoplist;
	private static boolean stoplistLoaded = false;
	
	private int vertex_id;
	private String term;
	private HashSet<String> publication_list;
	
	private Term(String term) {
        this.term = term;
        this.vertex_id = Integer.MIN_VALUE;
        this.publication_list = new HashSet<String>();
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
            t = new Term(term);
        }
        return t;
    }
	
	static public Term searchTerm(String term) {
        return termMap.get(term);
    }
	
	public int getVertexId() {
		if(this.vertex_id == Integer.MIN_VALUE)
			this.vertex_id = Processor.getNewVertexId();
    	return vertex_id;
    }
	
	public void addPublication(String publication) {
		publication_list.add(publication);
	}
	
	public HashSet<String> getAddedPublicationList() {
    	return publication_list;
    }
	
	public void removePublication(String publication) {
		publication_list.remove(publication);
		if(publication_list.isEmpty())
    		deleteTerm();
    }
	
	public void deleteTerm() {
		//remove this author from publication
    	for(String p: publication_list)
    		Publication.searchPublication(p).removeTerm(term);
    	termMap.put(term, null);
    }
	
	public String toString() {
        return "ID:" + getVertexId() + ";Type:Term;Key:" + term + ";Publications:" + publication_list;
    }
    
    /*
     * output: term_id paper1_id edge1_id paper2_id edge2_id ... 
     */
    public String getGraphLine() {
    	String graph = "" + getVertexId();
    	for(String publication: publication_list) {
    		int edge_id = Processor.getNewEdgeId();
        	Processor.setEdgeType(edge_id, Config.T2P);
    		graph += " " + Publication.searchPublication(publication).getVertexId() + " " + edge_id;
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
