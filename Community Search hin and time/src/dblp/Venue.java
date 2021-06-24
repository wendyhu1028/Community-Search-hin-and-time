package dblp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Venue {
	private static HashMap<String, String> edgeMap = new HashMap<String, String>();
	
	public void loadVolume(String url) {
        try {
            URL u = new URL(url);
            confParser.parse(u.openStream(), confHandler);    	            
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
            return;
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + url + " "+ e.getMessage());
            return;
        }
	}
        
	/*
     * Publiction information is loaded on demand only
     */    	
	static private SAXParser confParser;
    static private ConfConfigHandler confHandler;
    
    
    static private class ConfConfigHandler extends DefaultHandler {
    	
        private String key, year, name, pid, title;
        private HashSet<String> authors;
        private boolean insideYear, insideAuthor, insideKey, insideTitle;

        public void startElement(String namespaceURI, String localName,
                String rawName, Attributes atts) throws SAXException {        	
        	switch(rawName) {
	        	case "hit" :
		    		authors = new HashSet<String>();
		    		break;
	        	case "author" :
	        		if(authors!= null) {
		        		pid = atts.getValue("pid");
		        		authors.add(pid);
		        		name = "";
		        		insideAuthor = true;
	        		}
		    		break;
	        	case "year" :
	        		year = "";
		    		insideYear = true;
		    		break;
	        	case "key" :
	        		key = "";
	        		insideKey = true;
		    		break;
	        	case "title" :
	        		title = "";
	        		insideTitle = true;
		    		break;
        	}
        }

        public void endElement(String namespaceURI, String localName,
                String rawName) throws SAXException {
            switch(rawName) {
	        	case "hit" :
	        		Publication.create(key, year, authors, title);
	            	for(String author: authors)
	            		Person.searchPerson(author).addPubliction(key);
		    		break;
	        	case "author" :
	        		if(authors!= null) {
	        			Person.create(name, pid);
		        		insideAuthor = false;
	        		}
		    		break;
	        	case "year" :
		    		insideYear = false;
		    		break;
	        	case "key" :
	        		insideKey = false;
		    		break;
	        	case "title" :
	        		insideTitle = false;
		    		break;
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (insideYear)
            	year += new String(ch, start, length);
            if (insideAuthor)
            	name += new String(ch, start, length);
            if (insideKey)
            	key += new String(ch, start, length);
            if (insideTitle)
            	title += new String(ch, start, length);
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
        	confParser = SAXParserFactory.newInstance().newSAXParser();

        	confHandler = new ConfConfigHandler();
        	confParser.getXMLReader().setFeature(
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
    
    
}
