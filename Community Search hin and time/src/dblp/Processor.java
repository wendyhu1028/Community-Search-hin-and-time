package dblp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
 * Created on 2021.06.24 by Wenyi Hu
 */

public class Processor {
	
	//edge type
	public static final int P2A = 1;
	public static final int A2P = 2;
	
	//edge type
	public static final int AUTHOR = 0;
	public static final int PAPER = 1;
	
	private static int vertex_id = -1, edge_id = -1;
	private static int edgeType[] = new int[200000];
	
	
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
	
	private void produce_vldb_graph(List<String> conf_urls, String file_name) throws IOException {
		Venue v = new Venue();
		for(String url: conf_urls) {
			//System.out.println(url);
			v.loadVolume(url);
		}
		
		/* Graph.txt*/
		//output stream
		BufferedWriter graph = new BufferedWriter(new FileWriter(file_name + "\\Graph.txt", true));
		BufferedWriter info = new BufferedWriter(new FileWriter(file_name + "\\Info.txt", true));
		// person_id paper1_id edge1_id paper2_id edge2_id ... 
        for(Person person: Person.getAllPersons()) {
        	info.write(person + "\n");
        	graph.write(person.getGraphLine() + "\n");
        }
        
        // paper_id author1_id egde1_id author2_id egde2_id ...
        for(Publication publication: Publication.getAllPublications()) {
        	info.write(publication + "\n");
        	graph.write(publication.getGraphLine() + "\n");
        }
        
        //close output stream
        graph.flush();
        graph.close();
        info.flush();
        info.close();
        
        /* Vertex.txt*/
        //output stream
      	BufferedWriter vertex = new BufferedWriter(new FileWriter(file_name + "\\Vertex.txt", true));
        // vertex_id vertex_type(person-0; paper-1)
        for(Person person: Person.getAllPersons()) {
        	vertex.write(person.getVertexId() + " " + AUTHOR + "\n");
        }
        for(Publication publication: Publication.getAllPublications()) {
        	vertex.write(publication.getVertexId() + " " + PAPER + "\n");
        }
        vertex.flush();
        vertex.close();
        
        /* Vertex.txt*/
      //output stream
      	BufferedWriter edge = new BufferedWriter(new FileWriter(file_name + "\\Edge.txt", true));
        // dege_id edge_type
        for(int i = 0; i < edge_id+1; i++) {
        	edge.write(i + " " + edgeType[i] + "\n");
        }
        edge.flush();
        edge.close();
	}
	
//	private void read_stoplist(String file_name) throws IOException {
//		stoplist = new HashSet<String>();
//		BufferedReader stdin = new BufferedReader(new FileReader(file_name));
//		String line = null;
//		while((line = stdin.readLine()) != null){
//			stoplist.add(line);
//		}
//		stdin.close();
//	}
	
	public static void main(String[] args) {
		ArrayList<String> conf_urls = new ArrayList<String>();
		for(int i = 1; i < 2; i++) {
			conf_urls.add("https://dblp.uni-trier.de/search/publ/api?q=toc%3Adb/journals/pvldb/pvldb" + i + ".bht%3A&h=1000&format=xml");
		}
		
		try {
			Processor p = new Processor();
			p.produce_vldb_graph(conf_urls, "E:\\data\\dblp\\vldb");
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
