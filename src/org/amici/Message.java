package org.amici;

import il.technion.ewolf.kbr.Node;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.amici.utils.CertificateUtils;
import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonObject;

public class Message implements Serializable, Comparable<Message>{
	private static final long serialVersionUID = 3428485266944241572L;
	
	public static final String AUTHOR_TAG = "M";
	public static final String MENTION_TAG = "N";
	public static final String HASH_TAG = "H";
	
	private String author;
	private String text;
	private String signature = null;
	private long timeCreated = System.currentTimeMillis();
	private String hostingAddress;
	private String hostingMessage;
	
	
	public Message(String author, String text){
		this.author = author;
		this.text = text;
	}
	
	public String getJSON(){
		return getJSON( signature != null );
	}
	
	public String getAuthor(){
		return author;
	}

	public String getText(){
		return text;
	}
	
	public String getJSON( boolean includeSignature ){
		JsonObject json = new JsonObject();
		json.addProperty("a", author);
		json.addProperty("m", text);
		json.addProperty("t", timeCreated);
		if(includeSignature){
			json.addProperty("s", signature);
			json.addProperty("ha", hostingAddress);
			json.addProperty("hm", hostingMessage);
		}
		
		return json.toString();
	}
	
	public List<String> getTags(){
		List<String> list = new ArrayList<String>();
		Matcher matcher = Pattern.compile("#\\s*(\\S+)").matcher(text);
		while (matcher.find()) {
		  list.add(matcher.group(1));
		}
		return list;
	}
	
	public List<String> getMentions(){
		List<String> list = new ArrayList<String>();
		Matcher matcher = Pattern.compile("~\\s*(\\S+)").matcher(text);
		while (matcher.find()) {
		  list.add(matcher.group(1));
		}
		return list;
	}
	
	public void sign(PrivateKey key){
		signature = CertificateUtils.signData(getJSON(false), key);
	}

	public boolean verify(X509Certificate certificate){
		return CertificateUtils.checkSignature(getJSON(false), author, signature, certificate);
	}

	public String toString(){
		return getJSON( false );
	}
	
	public int compareTo(Message o) {
		return(new Long(this.timeCreated).compareTo(new Long(o.timeCreated)));
	}
	
	public void addHostingDetails(Node node, String message){
		this.hostingAddress = node.getInetAddress().toString() + "/" + node.getKey().toBase64();
		this.hostingMessage = message;
	}
	
	public String getBase64Hash(){
		try {
			return Base64.encodeBase64String( MessageDigest.getInstance("MD5").digest(toString().getBytes("UTF-8")) );
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return "ERRHASH";
		}
	}
	
	public boolean equals(Object obj){
		if( obj instanceof Message){
			return toString().equals( obj.toString() );
		}else{
			return this == obj;
		}
	}
	
}
