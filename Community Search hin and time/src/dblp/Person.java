package dblp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import util.Config;

/*
 * Created on 2021.06.24 by Wenyi Hu
 */

public class Person {
	private static Map<String, Person> personMap = new HashMap<String, Person>();

	private int vertex_id;
    private String name, pid;
    private HashSet<String> publication_list;
    private HashSet<String> venue_list;

    /*
     * Create a new Person object.
     */
    private Person(String name, String pid) {
        this.name = name;
        this.pid = pid;
        this.vertex_id = Integer.MIN_VALUE;
        this.publication_list = new HashSet<String>();
        this.venue_list = new HashSet<String>();
        personMap.put(pid, this);
    }
    
    /*
     * Create a new Person object if necessary.
     */
    static public Person create(String name, String pid) {
        Person p;
        p = searchPerson(pid);
        if (p == null) {
            p = new Person(name, pid);
        }
        return p;
    }
    
    /*
     * Check if a Person object already has been created. 
     */
    static public Person searchPerson(String pid) {
        return personMap.get(pid);
    }
    
    static public Collection<Person> getAllPersons() {
        return personMap.values();
    }
    
    public void deletePerson() {
    	//remove this author from publication
    	for(String p: publication_list)
    		Publication.searchPublication(p).removeAuthor(pid);
    	personMap.put(pid, null);
    }
    
    public String getName() {
        return name;
    }
    
    public int getVertexId() {
    	if(this.vertex_id == Integer.MIN_VALUE)
			this.vertex_id = Processor.getNewVertexId();
    	return vertex_id;
    }
    
    public String toString() {
        return "ID:" + getVertexId() + ";Type:Author;Key:" + pid + ";Name:" + name + ";Venues:" + venue_list + ";Publications:" + publication_list;
    }
    
    public void addPubliction(String publication) {
    	publication_list.add(publication);
    	venue_list.add(Publication.searchPublication(publication).getVenue());
    }
    
    public void removePublication(String publication) {
		publication_list.remove(publication);
    }
    
    
    public String getCommunityInfo() {
    	String community_info = "" + getVertexId();
    	for(String venue: venue_list) {
        	community_info += "," + venue;
        }
        return community_info;
    }
    
    public String getCommunityInfo2() {
    	HashSet<String> area_list = getArea();
    	String community_info = "" + getVertexId();
    	for(String area: area_list) {
        	community_info += "," + area;
        }
        return community_info;
    }
    
    public HashSet<String> getArea() {
    	HashSet<String> area_list = new HashSet<String>();
    	for(String venue: venue_list) {
    		area_list.add(Processor.venue2area.get(venue));
        }
        return area_list;
    }
    
    /*
     * output: person_id paper1_id edge1_id paper2_id edge2_id ... 
     */
    public String getGraphLine() {
    	String graph = "" + getVertexId();
    	for(String publication: publication_list) {
        	int edge_id = Processor.getNewEdgeId();
        	Processor.setEdgeType(edge_id, Config.A2P);
    		graph += " " + Publication.searchPublication(publication).getVertexId() + " " + edge_id;
        }
        return graph;
    }
    
    /*
     * Return added publication. 
     */
    public HashSet<String> getAddedPublicationList() {
    	return publication_list;
    }
    
    /*
     * Coauthor information is loaded on demand only
     */

    private boolean coauthorsLoaded;
    private Person coauthors[];

    static private SAXParser coauthorParser;
    static private CAConfigHandler coauthorHandler;
    static private List<Person> plist = new ArrayList<Person>();

    static private class CAConfigHandler extends DefaultHandler {

        private String Value, pid;
        private boolean insideAuthor;

        public void startElement(String namespaceURI, String localName,
                String rawName, Attributes atts) throws SAXException {
            if (insideAuthor = rawName.equals("author")) {
                Value = "";
                pid = atts.getValue("pid");
            }
        }

        public void endElement(String namespaceURI, String localName,
                String rawName) throws SAXException {
            if (rawName.equals("author") && Value.length() > 0) {
                plist.add(create(Value,pid));
//                System.out.println(pid + "   " + plist.size());
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (insideAuthor)
                Value += new String(ch, start, length);
        }

        private void Message(String mode, SAXParseException exception) {
            System.out.println(mode + " Line: " + exception.getLineNumber()
                    + " URI: " + exception.getSystemId() + "\n" + " Message: "
                    + exception.getMessage());
        }

        public void warning(SAXParseException exception) throws SAXException {

            Message("**Parsing Warning**\n", exception);
            throw new SAXException("Warning encountered");
        }

        public void error(SAXParseException exception) throws SAXException {

            Message("**Parsing Error**\n", exception);
            throw new SAXException("Error encountered");
        }

        public void fatalError(SAXParseException exception) throws SAXException {

            Message("**Parsing Fatal Error**\n", exception);
            throw new SAXException("Fatal Error encountered");
        }
    }

    static {
        try {
            coauthorParser = SAXParserFactory.newInstance().newSAXParser();

            coauthorHandler = new CAConfigHandler();
            coauthorParser.getXMLReader().setFeature(
                    "http://xml.org/sax/features/validation", false);

        } catch (ParserConfigurationException e) {
            System.out.println("Error in XML parser configuration: "
                    + e.getMessage());
            System.exit(1);
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage());
            System.exit(2);
        }
    }

    private void loadCoauthors() {
        if (coauthorsLoaded)
            return;
        plist.clear();
        try {            
            URL u = new URL("https://dblp.uni-trier.de/pid/" + pid
                    + ".xml?view=coauthor");
            coauthorParser.parse(u.openStream(), coauthorHandler);
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
            coauthors = new Person[0];
            return;
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + name + " "+ e.getMessage());
            coauthors = new Person[0];
            return;
        }
        coauthors = new Person[plist.size()];
        coauthors = plist.toArray(coauthors);
        coauthorsLoaded = true;
    }
    
    public Person[] getCoauthors() {
        if (!coauthorsLoaded) {
            loadCoauthors();
        }
        return coauthors;
    }
    
    /*
     * publication information is loaded on demand only
     */

    private boolean publLoaded;
    private Publication publications[];
    private Publication personRecord;

    static private SAXParser publParser;
    static private PublConfigHandler publHandler;
    static private List<Publication> publlist = new ArrayList<Publication>();
    static private Publication hp;

    static private class PublConfigHandler extends DefaultHandler {

        private String key, year, name, pid;
        private HashSet<String> authors;
        private boolean insideYear,insideAuthor;

        public void startElement(String namespaceURI, String localName,
                String rawName, Attributes atts) throws SAXException {
        	if (rawName.equals("inproceedings") || rawName.equals("article") || rawName.equals("proceedings") || rawName.equals("book")) {
	    		key = atts.getValue("key");
	    		authors = new HashSet<String>();
    		}
        	if (authors!= null && (insideAuthor = rawName.equals("author"))) {
        		pid = atts.getValue("pid");
        		authors.add(pid);
        		name = "";
        	}
        	if (insideYear = rawName.equals("year"))
        		year = "";
        }

        public void endElement(String namespaceURI, String localName,
                String rawName) throws SAXException {
            if (rawName.equals("inproceedings") || rawName.equals("article") || rawName.equals("proceedings") || rawName.equals("book")) {
                Publication p = Publication.create(key, year, authors,"NONE", "NONE");
                publlist.add(p);
            }
            if(rawName.equals("author"))
            	create(name, pid);
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (insideYear)
            	year += new String(ch, start, length);
            if (insideAuthor)
            	name += new String(ch, start, length);
        }

        private void Message(String mode, SAXParseException exception) {
            System.out.println(mode + " Line: " + exception.getLineNumber()
                    + " URI: " + exception.getSystemId() + "\n" + " Message: "
                    + exception.getMessage());
        }

        public void warning(SAXParseException exception) throws SAXException {

            Message("**Parsing Warning**\n", exception);
            throw new SAXException("Warning encountered");
        }

        public void error(SAXParseException exception) throws SAXException {

            Message("**Parsing Error**\n", exception);
            throw new SAXException("Error encountered");
        }

        public void fatalError(SAXParseException exception) throws SAXException {

            Message("**Parsing Fatal Error**\n", exception);
            throw new SAXException("Fatal Error encountered");
        }
    }

    static {
        try {
            publParser = SAXParserFactory.newInstance().newSAXParser();

            publHandler = new PublConfigHandler();
            publParser.getXMLReader().setFeature(
                    "http://xml.org/sax/features/validation", false);

        } catch (ParserConfigurationException e) {
            System.out.println("Error in XML parser configuration: "
                    + e.getMessage());
            System.exit(1);
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage());
            System.exit(2);
        }
    }

    private void loadPublications() {
        if (publLoaded)
            return;
        publlist.clear();
        hp = null;
        try {
        	URL u = new URL("https://dblp.org/pid/" + pid
                  + ".xml");
            publParser.parse(u.openStream(), publHandler);
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
            coauthors = new Person[0];
            return;
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + name + " "+ e.getMessage());
            coauthors = new Person[0];
            return;
        }
        publications = new Publication[publlist.size()];
        publications = publlist.toArray(publications);
        personRecord = hp;
        publLoaded = true;
    }

    public int getNumberOfPublications() {
        if (!publLoaded) {
            loadPublications();
        }
        return publications.length;
    }

    public Publication[] getPublications() {
        if (!publLoaded) {
            loadPublications();
        }
        return publications;
    }
    
    public Publication getPersonRecord() {
        if (!publLoaded) {
            loadPublications();
        }
        return personRecord;
    }
}
