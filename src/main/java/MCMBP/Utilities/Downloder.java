package MCMBP.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.io.FileUtils;



public class Downloder {


	public String Download(String Link, String FileType) throws IOException {
	
		
	String wget="";
	if(new File("/usr/bin/wget").exists())
		wget="/usr/bin/wget";
	if(new File("/usr/local/bin/wget").exists())
		wget="/usr/local/bin/wget";
	if(wget.isEmpty())
		System.out.println( "wget command can not be found. Please check if wget is already installed.");
	
		
	
	Runtime rt = Runtime.getRuntime();
	Process pr = rt.exec(wget+" "+Link+" -T 60 -t 3 -O "+Thread.currentThread().getId()+"."+FileType);
	BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		

			
String st="";
			while ((st = stdInput.readLine()) != null) {
				System.out.println(st);
			}
			stdInput.close();
			
			return Thread.currentThread().getId()+"."+FileType;
}
	
	public String GetHttpRequste(String urllink) throws IOException {
			
	URL url=null;
	try {
		url = new URL(urllink);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	HttpURLConnection conn=null;
	
	try {
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(10000);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		conn.setRequestMethod("GET");
	} catch (ProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		conn.disconnect();
	}
	conn.setRequestProperty("Accept", "application/xml");

	
	

	BufferedReader br=null;
	try {
		br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		
		conn.disconnect();
	}

	if(br==null)
		return"";
	
	String output;
	
	String Txt="";
	
	
	try {
		while ((output = br.readLine()) != null) {
			//System.out.println(output);
			Txt+=output+"\n";
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		conn.disconnect();
	}
	conn.disconnect();
	  	
	return Txt;
}
}
