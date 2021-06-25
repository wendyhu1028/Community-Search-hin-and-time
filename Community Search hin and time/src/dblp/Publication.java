package dblp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
 * Created on 2021.06.24 by Wenyi Hu
 */

public class Publication {
	private static Map<String, Publication> publicationMap = new HashMap<String, Publication>();

	private int vertex_id;
	private String year, title;
    private HashSet<String> authors, terms;
    private Map<String, Integer> edgeMap = new HashMap<String, Integer>();
    
    private Publication(String key, String year, HashSet<String> authors, String title, int vertex_id) {
        this.year = year;
        this.authors = authors; //pid
        this.title = title.substring(0, title.length()-1);
        this.vertex_id = vertex_id;
        this.terms = new HashSet<String>();
        publicationMap.put(key, this);
        
        //add authors neighbor
        for(String author: authors) {
        	int edge_id = Processor.getNewEdgeId();
        	edgeMap.put(author, edge_id);
        	Processor.setEdgeType(edge_id, Config.P2A);
        }
        
        //add terms neighbor
        for(String t: this.title.split(" ")) {
        	String t_lower = t.toLowerCase();
        	Term term = Term.create(t_lower);
        	if(term != null) {
        		term.addPublication(key);
        		terms.add(t_lower);
        		int edge_id = Processor.getNewEdgeId();
            	edgeMap.put(t_lower, edge_id);
            	Processor.setEdgeType(edge_id, Config.P2T);
        	}
        }
    }
     
    
    static public Publication create(String key, String year, HashSet<String> authors, String title) {
        Publication p;
        p = searchPublication(key);
        if (p == null) {
            p = new Publication(key, year, authors, title, Processor.getNewVertexId());
        }
        return p;
    }
    
    static public Publication searchPublication(String key) {
        return publicationMap.get(key);
    }
    
    static public Collection<Publication> getAllPublications() {
        return publicationMap.values();
    }
    
    public int getVertexId() {
    	return vertex_id;
    }
    
    public String toString() {
        return "ID: " + vertex_id + ", Year: " + year + ", Authors: " + authors +  ", Title: " + title + ", Terms: " + terms;
    }
    
    /*
     * output: paper_id author1_id egde1_id author2_id egde2_id ...
     */
    public String getGraphLine() {
    	String graph = "" + vertex_id;
    	for(String author: authors) {
    		graph += " " + Person.searchPerson(author).getVertexId() + " " + edgeMap.get(author);
        }
    	for(String term: terms) {
    		graph += " " + Term.searchTerm(term).getVertexId() + " " + edgeMap.get(term);
        }
        return graph;
    }
    
    public HashSet<String> getAuthors() {
        return authors;
    }
    
    public String getYear() {
        return year;
    }
    
    public static void main(String[] args) {
    	Person p1 = Person.create("Mark E. J. Newman", "n/MEJNewman");
    	Person p2 = Person.create("author2", "n/author");
    	
    	HashSet<String> authors = new HashSet<String>();
    	authors.add("n/MEJNewman");
    	authors.add("n/author");
    	
    	Publication pub = Publication.create("key01", "1990", authors, "title 01");
    	System.out.println(p1);
    	System.out.println(p2);
    	System.out.println(pub);
    	System.out.println(pub.getGraphLine());
    }
}
