package MCMBP.Resources;

import java.io.IOException;

import MCMBP.Utilities.Downloder;

public class elsevier {

	
	public String Get(String ElsevierToken, String doi) throws IOException {
		String PMC=new Downloder().GetHttpRequste("https://api.elsevier.com/content/article/doi/"+doi+"?APIKey="+ElsevierToken);
return PMC;
	}
}
