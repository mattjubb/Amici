package org.amici.messages;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.Node;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.amici.Amici;
import org.amici.CertificateUtils;
import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

public class Post implements Serializable, Comparable<Post>{
	private static final long serialVersionUID = 3428485266944241572L;
	public static final String AUTHOR_TAG = "M";
	public static final String MENTION_TAG = "N";
	public static final String HASH_TAG = "H";

	private static Gson gson = new Gson();
	private String author;
	private String text;
	private String signature = null;
	private long timeCreated = System.currentTimeMillis();
	private String hostingAddress = Amici.getIdentifier();
	private String hostingMessage = Amici.HOST_IDENTIFIER;
	
	
	public Post(String author, String text){
		this.author = author;
		this.text = text;
	}
	
	public String getAuthor(){
		return author;
	}

	public String getText(){
		return text;
	}
	
	public Date getDate(){
		return new Date(timeCreated);
	}
	
	public Long getTimeStamp(){
		return timeCreated;
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
	
	public String getDataToSign(){
		return author + text + timeCreated;
	}
	
	public void sign(PrivateKey key){
		signature = CertificateUtils.signData(getDataToSign(), key);
	}

	public boolean verify(X509Certificate certificate){
		return CertificateUtils.checkSignature(getDataToSign(), author, signature, certificate);
	}

	public String toString(){
		return gson.toJson(this);
	}
	
	public int compareTo(Post o) {
		return(new Long(this.timeCreated).compareTo(new Long(o.timeCreated)));
	}
	
	public void addHostingDetails(Node node, String message){
		hostingAddress = node.getInetAddress().toString() + "/" + node.getKey().toBase64();
		hostingMessage = message;
	}
	
	public String getHostingAddress(){
		return hostingAddress;
	}
	
	public String getHostingMessage(){
		return hostingMessage;
	}
	
	public String getBase64Hash(){
		try {
			return Base64.encodeBase64String( MessageDigest.getInstance("MD5").digest(toString().getBytes("UTF-8")) );
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return "ERRHASH";
		}
	}
	
	public Set<Key> getKeys(){
		Set<Key> keys = new HashSet<Key>();
		keys.add(Amici.getServer().getKeyFactory().create(getAuthor()));
		
		for(String tag:getTags())
			keys.add(Amici.getServer().getKeyFactory().create(tag));
		
		for(String mention:getMentions())
			keys.add(Amici.getServer().getKeyFactory().create(mention));
		
		return keys;
	}
	
	public boolean equals(Object obj){
		if( obj instanceof Post){
			return toString().equals( obj.toString() );
		}else{
			return this == obj;
		}
	}
	
	public static Post fromJson(String json){
		return gson.fromJson(json, Post.class);
	}
}
