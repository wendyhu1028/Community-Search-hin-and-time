package dblp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import util.Config;

/*
 * Created on 2021.06.24 by Wenyi Hu
 */

public class Publication {
	private static Map<String, Publication> publicationMap = new HashMap<String, Publication>();

	private int vertex_id;
	private String year, title, venue, key;
    private HashSet<String> authors, terms;
    
    private Publication(String key, String year, HashSet<String> authors, String title, String venue) {
    	this.key = key;
    	this.year = year;
        this.authors = authors; //pid
        this.title = title.substring(0, title.length()-1);
        this.venue = venue;
        this.vertex_id = Integer.MIN_VALUE;
        this.terms = new HashSet<String>();
        publicationMap.put(key, this);
        
        //add authors neighbor
        for(String author: authors) {
        	Person.searchPerson(author).addPubliction(key); //add p2a
        }
        
        //add terms neighbor
        for(String t: this.title.split(" ")) {
        	String t_processed = processTerm(t);
        	Term term = Term.create(t_processed);
        	if(term != null) {
        		term.addPublication(key); //add p2t
        		terms.add(t_processed);
        	}
        }
        
        //add venue neighbor
        Venue.searchVenue(venue).addPubliction(key); // add p2v
    }
     
    static public Publication create(String key, String year, HashSet<String> authors, String title, String venue) {
        Publication p;
        p = searchPublication(key);
        if (p == null) {
            p = new Publication(key, year, authors, title, venue);
        }
        return p;
    }
    
    static public Publication searchPublication(String key) {
        return publicationMap.get(key);
    }
    
    static public Collection<Publication> getAllPublications() {
        return publicationMap.values();
    }
    
    public void removeAuthor(String pid) {
    	authors.remove(pid);
    	if(authors.isEmpty())
    		deletePublication();
    }
    
    public void removeTerm(String term) {
    	terms.remove(term);
    }
    
    public void deletePublication() {
    	//remove this publication from author
    	for(String a: authors)
    		Person.searchPerson(a).removePublication(key);
    	//remove this publication from term
    	for(String t: terms)
    		Term.searchTerm(t).removePublication(key);
    	//remove this publication from venue
    	Venue.searchVenue(venue).removePublication(key);
    	
    	publicationMap.put(key, null);
    }
    
    public int getVertexId() {
    	if(this.vertex_id == Integer.MIN_VALUE)
			this.vertex_id = Processor.getNewVertexId();
    	return vertex_id;
    }
    
    public String toString() {
        return "ID:" + getVertexId() + ";Type:Paper;Key:"+ key +";Venue:" + venue + ";Year:" + year +  ";Title:" + title + ";Authors:" + authors + ";Terms:" + terms;
    }
    
    /*
     * output: paper_id venue_id edge_id author1_id egde1_id author2_id egde2_id ...
     */
    public String getGraphLine() {
        int edge_id = Processor.getNewEdgeId();
    	Processor.setEdgeType(edge_id, Config.P2V);
    	String graph = getVertexId() + " " +Venue.searchVenue(venue).getVertexId() + " " + edge_id;
    	for(String author: authors) {
    		edge_id = Processor.getNewEdgeId();
        	Processor.setEdgeType(edge_id, Config.P2A);
    		graph += " " + Person.searchPerson(author).getVertexId() + " " + edge_id;
        }
    	for(String term: terms) {
    		edge_id = Processor.getNewEdgeId();
        	Processor.setEdgeType(edge_id, Config.P2T);
    		graph += " " + Term.searchTerm(term).getVertexId() + " " + edge_id;
        }
        return graph;
    }
    
    public HashSet<String> getAuthors() {
        return authors;
    }
    
    public String getYear() {
        return year;
    }
    
    public String getVenue() {
        return venue;
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
