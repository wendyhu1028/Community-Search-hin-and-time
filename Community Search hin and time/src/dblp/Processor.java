package dblp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Config;

/*
 * Created on 2021.06.24 by Wenyi Hu
 */

public class Processor {

	private static int vertex_id = -1, edge_id = -1;
	private static int edgeType[] = new int[2000000];
	
	/*
     * Get vertex id and auto-increment
     */
	public static int getNewVertexId() {
		vertex_id++;
		return vertex_id;
	}
	
	/*
     * Get edge id and auto-increment
     */
	public static int getNewEdgeId() {
		edge_id++;
		return edge_id;
	}
	
	public static void setEdgeType(int id, int type) {
		edgeType[id] = type;
	}

	/*
     * Greate 2-hub graph.
     */
	private void produce_2_hub_graph(Person person, String file_name) throws IOException {
		//output stream
		BufferedWriter stdout = new BufferedWriter(new FileWriter(file_name, true));
		stdout.write("v1;v2;year");
		stdout.newLine();
		
		//1-hub
		//person.getCoauthors();
		Publication publications[] = person.getPublications();
		for(Publication pub: publications) {
			ArrayList<String> authors_recorded = new ArrayList<String>();
        	for(String author1: pub.getAuthors()) {
        		for(String author2: pub.getAuthors()) {
        			if(author1.equals(author2) || authors_recorded.contains(author2))
        				continue;
        			stdout.write(Person.searchPerson(author1).getName() + "," + Person.searchPerson(author2).getName() + "," + pub.getYear());
        		}
        		authors_recorded.add(author1);
        	}
        }
		
		//2-hub
		for(Person p2: person.getCoauthors()) {
			//p2.getCoauthors();
			publications = p2.getPublications();
			for(Publication pub: publications) {
				ArrayList<String> authors_recorded = new ArrayList<String>();
	        	for(String author1: pub.getAuthors()) {
	        		for(String author2: pub.getAuthors()) {
	        			if(author1.equals(author2) || authors_recorded.contains(author2))
	        				continue;
	        			stdout.write(Person.searchPerson(author1).getName() + "," + Person.searchPerson(author2).getName() + "," + pub.getYear());
	        		}
	        		authors_recorded.add(author1);
	        	}
	        }
		}
		//close output stream
		stdout.flush();
		stdout.close();
	}
	
	private void produce_vldb_graph(HashMap<String, ArrayList<String>> venue2urls, String file_name) throws IOException {
		for(Map.Entry<String, ArrayList<String>> entry : venue2urls.entrySet()) {
			Venue v = new Venue(entry.getKey(), entry.getValue());
		}
		
		/*modify graph*/
		//delete author only published one paper
		for(Person p: Person.getAllPersons()) {
        	if(p.getAddedPublicationList().size()<2)
        		p.deletePerson();
        }
		
		/* Graph.txt*/
		//output stream
		BufferedWriter graph = new BufferedWriter(new FileWriter(file_name + "\\Graph.txt", true));
		BufferedWriter info = new BufferedWriter(new FileWriter(file_name + "\\Info.txt", true));
		BufferedWriter community = new BufferedWriter(new FileWriter(file_name + "\\Community.txt", true));
		// person_id paper1_id edge1_id paper2_id edge2_id ... 
        for(Person person: Person.getAllPersons()) {
        	if(person == null)
        		continue;
        	info.write(person + "\n");
        	graph.write(person.getGraphLine() + "\n");
        	community.write(person.getCommunityInfo() + "\n");
        }
        
        // paper_id venue_id edge_id author1_id egde1_id author2_id egde2_id ... term1_id edge1_id term2_id edge2_id
        for(Publication publication: Publication.getAllPublications()) {
        	if(publication == null)
        		continue;
        	info.write(publication + "\n");
        	graph.write(publication.getGraphLine() + "\n");
        }
        
        // term_id paper1_id edge1_id paper2_id edge2_id ... 
        for(Term term: Term.getAllTerms()) {
        	if(term == null)
        		continue;
        	info.write(term + "\n");
        	graph.write(term.getGraphLine() + "\n");
        }
        
        // venue_id paper1_id edge1_id paper2_id edge2_id ... 
        for(Venue venue: Venue.getAllVenues()) {
        	info.write(venue + "\n");
        	graph.write(venue.getGraphLine() + "\n");
        }
        
        //close output stream
        graph.flush();
        graph.close();
        info.flush();
        info.close();
        community.flush();
        community.close();
        
        /* Vertex.txt*/
        //output stream
      	BufferedWriter vertex = new BufferedWriter(new FileWriter(file_name + "\\Vertex.txt", true));
        // vertex_id vertex_type(person-0; paper-1)
        for(Person person: Person.getAllPersons()) {
        	if(person == null)
        		continue;
        	vertex.write(person.getVertexId() + " " + Config.AUTHOR + "\n");
        }
        for(Publication publication: Publication.getAllPublications()) {
        	if(publication == null)
        		continue;
        	vertex.write(publication.getVertexId() + " " + Config.PAPER + "\n");
        }
        for(Term term: Term.getAllTerms()) {
        	if(term == null)
        		continue;
        	vertex.write(term.getVertexId() + " " + Config.TERM + "\n");
        }
        for(Venue venue: Venue.getAllVenues()) {
        	vertex.write(venue.getVertexId() + " " + Config.VENUE + "\n");
        }
        vertex.flush();
        vertex.close();
        
        /* Edge.txt*/
      //output stream
      	BufferedWriter edge = new BufferedWriter(new FileWriter(file_name + "\\Edge.txt", true));
        // dege_id edge_type
        for(int i = 0; i < edge_id+1; i++) {
        	edge.write(i + " " + edgeType[i] + "\n");
        }
        edge.flush();
        edge.close();
	}
	
	private static HashMap<String, ArrayList<String>> produce_venue_urls() {
		HashMap<String, ArrayList<String>> venue2urls = new HashMap<String, ArrayList<String>>();
		
		/* Database 2015~2016*/
//		//vldb
		ArrayList<String> urls = new ArrayList<String>();
		for(int i = 8; i < 11; i++) {
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/journals/pvldb/pvldb" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("VLDB", urls);
		//icde
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/icde/icde2015.bht%3A&h=1000&format=xml
			//	        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/icde/icde
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/series/sci/sci447.bht%3A&h=1000&format=xml			
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/icde/icde" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("ICDE", urls);
		//sigmod
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/sigmod/sigmod2015.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/sigmod/sigmod" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("SIGMOD", urls);
		
		//AI
		//t-pami
		urls = new ArrayList<String>();
		for(int i = 37; i < 39; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/journals/pami/pami37.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/journals/pami/pami" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("PAMI", urls);
		//AAAI
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/aaai/aaai2015.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/aaai/aaai" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("AAAI", urls);
		//ijcai
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/ijcai/ijcai2015.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/ijcai/ijcai" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("IJCAI", urls);
		
		//computer vision
		//cvpr
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/cvpr/cvpr2015.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/cvpr/cvpr" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("CVPR", urls);
		//iccv
		urls = new ArrayList<String>();
		for(int i = 2015; i < 2017; i++) {
			//        https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/iccv/iccv2015.bht%3A&h=1000&format=xml
			urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/conf/iccv/iccv" + i + ".bht%3A&h=1000&format=xml");
		}
		venue2urls.put("ICCV", urls);
		
		return venue2urls;
	}
	
	public static void main(String[] args) {
		HashMap<String, ArrayList<String>> venue2urls = produce_venue_urls();
				
		try {
			Processor p = new Processor();
			p.produce_vldb_graph(venue2urls, Config.outputPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
//		//test create 2-hub graph
//		Person p1;
//		p1 = Person.create("Mark E. J. Newman", "n/MEJNewman");
//		try {
//				Processor p = new Processor();
//				p.produce_2_hub_graph(p1, "E:\\data\\dblp\\hin_mark_newman_full.csv");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		
//		Person coauthors[] = p1.getCoauthors();
//		System.out.println("number of coauthors: " + coauthors.length);
//		for(Person ca: coauthors) {
//			System.out.println(ca);
//		}
//		
//		Publication publications[] = p1.getPublications();
//		System.out.println("number of publications: " + publications.length);
//		for(Publication pub: publications) {
//			System.out.println(pub);
//		}
    }
}
