package org.amici.server;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;

import java.security.cert.X509Certificate;
import java.util.*;

import org.amici.Amici;
import org.amici.Message;
import org.amici.server.handlers.CertificateHandler;
import org.amici.utils.CertificateUtils;

public class BasicDataStoreImpl implements DataStore {
	private Map<String,X509Certificate> certificates = new HashMap<String,X509Certificate>();
	private Map<String,Set<Message>> authoredMessages = new HashMap<String,Set<Message>>();
	private Map<String,Set<Message>> mentionedMessages = new HashMap<String,Set<Message>>();
	private Map<String,Set<Message>> taggedMessages = new HashMap<String,Set<Message>>();
	
	public boolean registerCertificate(String email, X509Certificate certificate) {
		if(CertificateUtils.isTrusted(certificate, email)){
			System.out.println("Registering: " + email);
			certificates.put(email, certificate);
			return true;
		}else return false;
	}

	public X509Certificate getCertificate(String email) {
		return certificates.get(email);
	}
	
	private void putMessageIntoMap(Map<String,Set<Message>> map, String key, Message message){
		Set<Message> list = map.get(key);
		if( list == null ){
			list = new HashSet<Message>();
			map.put(key,list);
		}
		System.out.println("Mapping("+Amici.getIdentifier()+"): " + key + " to " + message);
		list.add(message);
	}
	
	public void addMessage(Message message) {
		message.addHostingDetails(Amici.getServer().getRouter().getLocalNode(), Amici.HOST_IDENTIFIER);
		putMessageIntoMap( authoredMessages, message.getAuthor(), message);

		for(String tag:message.getTags())
			putMessageIntoMap( taggedMessages, tag, message);
		
		for(String mention:message.getMentions())
			putMessageIntoMap( mentionedMessages, mention, message);
	}

	@Override
	public Set<Message> collectCloseMessages(KeyComparator comparator, Key thisNode) {
		Set<Message> result = new HashSet<Message>();
		
	    Iterator<String> authIterator= authoredMessages.keySet().iterator();
	    while(authIterator.hasNext()){
	    	String key = authIterator.next();	
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
		    	result.addAll(authoredMessages.get(key));
	    }
	    	
	    
	    Iterator<String> tagIterator= taggedMessages.keySet().iterator();
	    while(tagIterator.hasNext()){
	    	String key = tagIterator.next();		
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
		    	result.addAll(taggedMessages.get(key));
	    }
	    
	    Iterator<String> menIterator= mentionedMessages.keySet().iterator();
	    while(menIterator.hasNext()){
	    	String key = menIterator.next();	
	    	if( comparator.compare(thisNode, Amici.getServer().getKeyFactory().create(key)) == -1 )
		    	result.addAll(mentionedMessages.get(key));
	    }
	    
	    return result;
	}

	public Set<Message> getUserFeed(String user, int count) {
		Set<Message> result = new TreeSet<Message>();
		
		if(!authoredMessages.containsKey(user))
			return result;
		
		Iterator <Message> it = authoredMessages.get(user).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}

	public Set<Message> getHashTagFeed(String hashtag, int count) {
		Set<Message> result = new TreeSet<Message>();
		
		if(!authoredMessages.containsKey(hashtag))
			return result;
		
		Iterator <Message> it = taggedMessages.get(hashtag).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}

	public Set<Message> getMentionFeed(String user, int count) {
		Set<Message> result = new TreeSet<Message>();
		
		if(!authoredMessages.containsKey(user))
			return result;
		
		Iterator <Message> it = mentionedMessages.get(user).iterator();
		int i = 0;
		while( i++ < count && it.hasNext() )
			result.add(it.next());
		return result;
	}
}
