package org.amici;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class TestUtils {
	public static String doGet(URL url){
	    StringBuffer result = new StringBuffer();
		try {
		    // Read all the text returned by the server
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	result.append(str);
		    }
		    in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return result.toString();
	}

	public static String doPost(URL url, String data){
	    StringBuffer result = new StringBuffer();
		try {
		    // Read all the text returned by the server
			URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    
		    String str;
		    while ((str = rd.readLine()) != null) {
		    	result.append(str);
		    }
		    rd.close();
		    wr.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	

	public static String doPost(URL url, Map<String,String> data){
		try{
			StringBuffer dataSB = new StringBuffer();
			Iterator<String> it = data.keySet().iterator();
		    while (it.hasNext()) {
		    	String key = it.next();
		    	dataSB.append(URLEncoder.encode(key, "UTF-8"));
		    	dataSB.append("=");
		    	dataSB.append(data.get(key));
		    }
		    return doPost(url,dataSB.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
