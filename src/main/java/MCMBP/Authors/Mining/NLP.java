package MCMBP.Authors.Mining;

import java.util.Properties;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NLP {

	String TokenTypeCOUNTRY(String Txt) {
		
	        Properties props = new Properties();
	        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref,quote");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
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
