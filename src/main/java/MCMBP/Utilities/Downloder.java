package MCMBP.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;



public class Downloder {


	public String Download(String Link, String FileName, String FileType) throws IOException {
		
		if(new File(FileName+"."+FileType).exists())// in case of crash and the old file did not delete
			FileUtils.deleteQuietly(new File(FileName+"."+FileType));
		
		
		URL hh= new URL(Link);
		URLConnection connection = hh.openConnection();
		String redirect = connection.getHeaderField("Location");
		
		if (redirect == null) { // if redirect is null, meaning that the website did not redirect to another link  
			redirect=Link;
		}
		
		
		
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		
		HttpGet httpget = new HttpGet(redirect);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
		BufferedInputStream bis = new BufferedInputStream(entity.getContent());
		String filePath = FileName+"."+FileType;
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
		int inByte;
		while((inByte = bis.read()) != -1) bos.write(inByte);
		bis.close();
		bos.close();
		
		
		/*
		//System.setProperty("Referer","https://www.google.com");
		URL hh= new URL(Link);
		URLConnection connection = hh.openConnection();
		String redirect = connection.getHeaderField("Location");
		
		if (redirect == null) { // if redirect is null, meaning that the website did not redirect to another link  
			redirect=Link;
		}
	
		URL url = new URL(redirect);
		
		InputStream in = url.openStream();
		FileOutputStream fos = new FileOutputStream(new File(Thread.currentThread().getId()+"."+FileType));

		
		int length = -1;
		byte[] buffer = new byte[1024];// buffer for portion of data from connection
		while ((length = in.read(buffer)) > -1) {
		    fos.write(buffer, 0, length);
		}
		fos.close();
		in.close();
		System.out.println("File downloaded");
		*/
		
		/*
		System.out.println("Download "+Link);
	String wget="";
	if(new File("/usr/bin/wget").exists())
		wget="/usr/bin/wget";
	if(new File("/usr/local/bin/wget").exists())
		wget="/usr/local/bin/wget";
	if(wget.isEmpty())
		System.out.println( "wget command can not be found. Please check if wget is already installed.");
	
		
	
	Runtime rt = Runtime.getRuntime();
	System.out.println("Runtime rt = Runtime.getRuntime(); "+Link);
	Process pr = rt.exec(wget+" "+Link+" -T 500 -t 3 -O "+Thread.currentThread().getId()+"."+FileType);
	System.out.println("Process pr "+Link);
	BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	System.out.println("BufferedReader stdInput "+Link);
	BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
	System.out.println("BufferedReader stdError "+Link);
	
String st="";
			while ((st = stdInput.readLine()) != null) {
				System.out.println("here "+Link);
				System.out.println(st);
			}
			System.out.println("loop "+Link);
			while ((st = stdError.readLine()) != null) {

				System.out.println("error "+st);
				

			}
			System.out.println("loop2 "+Link);
			
			System.out.println("Downloaded "+Link);
			stdError.close();
			stdInput.close();
			*/
			return FileName+"."+FileType;
}
	
	public String GetHttpRequste(String urllink) throws IOException {
		System.out.println("GetHttpRequste "+urllink);	
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
	  	
	System.out.println("GetHttpRequste done "+urllink);	
	return Txt;
}
}
