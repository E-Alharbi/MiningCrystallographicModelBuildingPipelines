package MCMBP.Authors.Mining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.GeoInfo;
import MCMBP.Utilities.TxtFiles;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public  class NLP {
	static  StanfordCoreNLP pipeline;
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException{
		
		
					

		
	}
	static void LoadNLP() {
		    Properties props = new Properties();
	   //     props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref,quote");
	        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");

	         pipeline = new StanfordCoreNLP(props);
	         
	       
	}
	static synchronized String TokenTypeCOUNTRY(String Txt , String ApplicationIdBack4app , String APIKeyBack4app) throws UnsupportedEncodingException, MalformedURLException {
		
	    
	        CoreDocument document = new CoreDocument(Txt);
	        pipeline.annotate(document);
	        
	        // get confidences for entities
	        String COUNTRY="";
	       
	        for (CoreEntityMention em : document.entityMentions()) {
	          
	        	
	            if(em.entityType().equals("COUNTRY"))
	            	COUNTRY=em.text();
	            
	        }
	        
	        if(COUNTRY.trim().length()==0) {
	        for (CoreEntityMention em : document.entityMentions()) {
	        	
	        	if(em.entityType().equals("CITY")||em.entityType().equals("STATE_OR_PROVINCE") || em.entityType().equals("LOCATION")) {
	        		
	        	COUNTRY=new GeoInfo().country(em.text(), ApplicationIdBack4app ,  APIKeyBack4app);
	        }
	             if(COUNTRY!=null && COUNTRY.trim().length()!=0) // if we get the country, we did not need to continue 
	            	break;
	        }
	        }
	       
	       	
	        return COUNTRY;
	}
}
