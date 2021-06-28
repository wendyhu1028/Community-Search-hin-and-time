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
	private String year, title, venue;
    private HashSet<String> authors, terms;
    private Map<String, Integer> edgeMap = new HashMap<String, Integer>();
    
    private Publication(String key, String year, HashSet<String> authors, String title, String venue, int vertex_id) {
        this.year = year;
        this.authors = authors; //pid
        this.title = title.substring(0, title.length()-1);
        this.venue = venue;
        this.vertex_id = vertex_id;
        this.terms = new HashSet<String>();
        publicationMap.put(key, this);
        
        //add authors neighbor
        for(String author: authors) {
        	Person.searchPerson(author).addPubliction(key); //add p2a
        	int edge_id = Processor.getNewEdgeId();
        	edgeMap.put(author, edge_id);
        	Processor.setEdgeType(edge_id, Config.P2A);
        }
        
        //add terms neighbor
        for(String t: this.title.split(" ")) {
        	String t_processed = processTerm(t);
        	Term term = Term.create(t_processed);
        	if(term != null && !edgeMap.containsKey(t_processed)) {
        		term.addPublication(key); //add p2t
        		terms.add(t_processed);
        		int edge_id = Processor.getNewEdgeId();
            	edgeMap.put(t_processed, edge_id);
            	Processor.setEdgeType(edge_id, Config.P2T);
        	}
        }
        
        //add venue neighbor
        Venue.searchVenue(venue).addPubliction(key); // add p2v
        int edge_id = Processor.getNewEdgeId();
    	edgeMap.put(venue, edge_id);
    	Processor.setEdgeType(edge_id, Config.P2V);
    }
     
    static public Publication create(String key, String year, HashSet<String> authors, String title, String venue) {
        Publication p;
        p = searchPublication(key);
        if (p == null) {
            p = new Publication(key, year, authors, title, venue, Processor.getNewVertexId());
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
        return "ID: " + vertex_id + ", Venue: " + venue + ", Year: " + year + ", Authors: " + authors +  ", Title: " + title + ", Terms: " + terms;
    }
    
    /*
     * output: paper_id venue_id edge_id author1_id egde1_id author2_id egde2_id ...
     */
    public String getGraphLine() {
    	String graph = vertex_id + " " +Venue.searchVenue(venue).getVertexId() + " " + edgeMap.get(venue);
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
    
    private String processTerm(String term) {
    	String processed_term = term.toLowerCase();
    	if(processed_term.startsWith("\"")||processed_term.startsWith("("))
    		processed_term = processed_term.substring(1);
    	if(processed_term.endsWith(".")||processed_term.endsWith(",")||processed_term.endsWith("\"")||processed_term.endsWith(")")
    			||processed_term.endsWith("!")||processed_term.endsWith("?"))
    		processed_term = processed_term.substring(0, processed_term.length()-1);
    	if(processed_term.endsWith("'s"))
    		processed_term = processed_term.substring(0, processed_term.length()-2);
    	return processed_term;
    }
    
    public static void main(String[] args) {
    	System.out.println("a.\"".endsWith("\""));
    	Person p1 = Person.create("Mark E. J. Newman", "n/MEJNewman");
    	Person p2 = Person.create("author2", "n/author");
    	
    	HashSet<String> authors = new HashSet<String>();
    	authors.add("n/MEJNewman");
    	authors.add("n/author");
    	
    	Publication pub = Publication.create("key01", "1990", authors, "title 01", "venue");
    	System.out.println(p1);
    	System.out.println(p2);
    	System.out.println(pub);
    	System.out.println(pub.getGraphLine());
    }
}
