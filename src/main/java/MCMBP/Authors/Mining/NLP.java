package MCMBP.Authors.Mining;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


import MCMBP.Utilities.Downloder;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public  class NLP {
	static  StanfordCoreNLP pipeline;
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException{
		
		String json =  new Downloder().GetHttpRequste("https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=32365117&resultType=core&format=json");
		
		LoadNLP();
	
		
		
		
		
		List<String> authors=new PMCMultiThreaded().ListofAffiliation(json);
					
				
					for(int a=0 ;a < authors.size(); ++a) {

					String val=TokenTypeCOUNTRY(authors.get(a).toString());
					System.out.print(val);
}
		
	}
	static void LoadNLP() {
		    Properties props = new Properties();
	        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref,quote");

	         pipeline = new StanfordCoreNLP(props);
	         
	       
	}
	static synchronized String TokenTypeCOUNTRY(String Txt) {
		
	     
	        CoreDocument document = new CoreDocument(Txt);
	        pipeline.annotate(document);
	        
	        // get confidences for entities
	        String COUNTRY="";
	        for (CoreEntityMention em : document.entityMentions()) {
	           // System.out.println(em.text() + "\t" + em.entityTypeConfidences());
	          // System.out.println(em.text() + "\t" + em.entityType());
	           // System.out.println(em.text() + "\t" + em.entity());
	            if(em.entityType().equals("COUNTRY"))
	            	COUNTRY=em.text();
	            
	        }
	       
	       	
	        return COUNTRY;
	}
}
