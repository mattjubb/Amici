package org.amici.server;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;

import java.security.cert.X509Certificate;
import java.util.*;

import org.amici.Amici;
import org.amici.CertificateUtils;
import org.amici.messages.Dump;
import org.amici.messages.Post;

public class BasicDataStoreImpl implements DataStore {
	private Map<String,X509Certificate> certificates = new HashMap<String,X509Certificate>();
	private Map<String,Set<Post>> authoredMessages = new HashMap<String,Set<Post>>();
	private Map<String,Set<Post>> mentionedMessages = new HashMap<String,Set<Post>>();
	private Map<String,Set<Post>> taggedMessages = new HashMap<String,Set<Post>>();
	
	public boolean registerCertificate(String email, X509Certificate certificate) {
		if(CertificateUtils.isTrusted(certificate, email)){
			Amici.getLogger(BasicDataStoreImpl.class).trace("Registering: "+email);
			certificates.put(email, certificate);
			return true;
		}else return false;
	}

	public X509Certificate getCertificate(String email) {
		return certificates.get(email);
	}
	
	private void putMessageIntoMap(Map<String,Set<Post>> map, String key, Post message){
		Set<Post> list = map.get(key);
		if( list == null ){
			list = new HashSet<Post>();
			map.put(key,list);
		}
		Amici.getLogger(BasicDataStoreImpl.class).trace("Mapping("+Amici.getIdentifier()+"): " + key + " to " + message.getBase64Hash());
		list.add(message);
	}
	
	public void addMessage(Post message) {
		message.addHostingDetails(Amici.getServer().getRouter().getLocalNode(), Amici.HOST_IDENTIFIER);
		putMessageIntoMap( authoredMessages, message.getAuthor(), message);

		for(String tag:message.getTags())
			putMessageIntoMap( taggedMessages, tag, message);
		
		for(String mention:message.getMentions())
			putMessageIntoMap( mentionedMessages, mention, message);
	}

	@Override
	public Dump collectDump(KeyComparator comparator, Key thisNode) {
		Dump dump = new Dump();
		
	    Iterator<String> authIterator= authoredMessages.keySet().iterator();
	    while(authIterator.hasNext()){
	    	String key = authIterator.next();	
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
	    		dump.addAll(authoredMessages.get(key));
	    }
	    	
	    
	    Iterator<String> tagIterator= taggedMessages.keySet().iterator();
	    while(tagIterator.hasNext()){
	    	String key = tagIterator.next();		
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
	    		dump.addAll(taggedMessages.get(key));
	    }
	    
	    Iterator<String> menIterator= mentionedMessages.keySet().iterator();
	    while(menIterator.hasNext()){
	    	String key = menIterator.next();	
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
	    		dump.addAll(mentionedMessages.get(key));
	    }
	    
	    return dump;
	}
	
	public void addDump(Dump dump){
		Iterator<Post> postIterator = dump.getContents().iterator();
		while(postIterator.hasNext())
			addMessage(postIterator.next());
	}

	public Set<Post> getUserFeed(String user, long since, long until, int count) {
		Set<Post> result = new TreeSet<Post>();
		
		if(!authoredMessages.containsKey(user))
			return result;
		
		Iterator <Post> it = authoredMessages.get(user).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}

	public Set<Post> getHashTagFeed(String hashtag, long since, long until, int count) {
		Set<Post> result = new TreeSet<Post>();
		
		if(!taggedMessages.containsKey(hashtag))
			return result;
		
		Iterator <Post> it = taggedMessages.get(hashtag).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}

	public Set<Post> getMentionFeed(String user, long since, long until, int count) {
		Set<Post> result = new TreeSet<Post>();
		
		if(!mentionedMessages.containsKey(user))
			return result;
		
		Iterator <Post> it = mentionedMessages.get(user).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}
}
