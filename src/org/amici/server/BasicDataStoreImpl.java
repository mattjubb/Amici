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
	private Map<String,List<X509Certificate>> certificates = new HashMap<String,List<X509Certificate>>();
	private Map<String,Set<Post>> authoredMessages = new HashMap<String,Set<Post>>();
	private Map<String,Set<Post>> mentionedMessages = new HashMap<String,Set<Post>>();
	private Map<String,Set<Post>> taggedMessages = new HashMap<String,Set<Post>>();
	
	public boolean registerCertificate(String email, X509Certificate certificate) {
		if(CertificateUtils.isTrusted(certificate, email)){
			Amici.getLogger(BasicDataStoreImpl.class).trace("Registering: "+email);
			if(certificates.containsKey(email))
				certificates.get( email ).add(certificate);
			else {
				List<X509Certificate> list = new LinkedList<X509Certificate>();
				list.add(certificate);
				certificates.put(email, list);
			}
			return true;
		}else return false;
	}

	public X509Certificate getCertificate(String email, Date date) {
		List<X509Certificate> possibles = certificates.get(email);
		for(X509Certificate cert:possibles){
			if( date.before(cert.getNotAfter()) && date.after(cert.getNotBefore()) ) 
				return cert;
		}
		return null;
	}
	
	private void putPostIntoMap(Map<String,Set<Post>> map, String key, Post post){
		Set<Post> list = map.get(key);
		if( list == null ){
			list = new HashSet<Post>();
			map.put(key,list);
		}
		Amici.getLogger(BasicDataStoreImpl.class).trace("Mapping("+Amici.getIdentifier()+"): " + key + " to " + post.getBase64Hash());
		list.add(post);
	}
	
	public void addPost(Post post) {
		post.addHostingDetails(Amici.getServer().getRouter().getLocalNode(), Amici.HOST_IDENTIFIER);
		putPostIntoMap( authoredMessages, post.getAuthor(), post);

		for(String tag:post.getTags())
			putPostIntoMap( taggedMessages, tag, post);
		
		for(String mention:post.getMentions())
			putPostIntoMap( mentionedMessages, mention, post);
	}
	
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
			addPost(postIterator.next());
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
	
	public void clear(){
		certificates = new HashMap<String,List<X509Certificate>>();
		authoredMessages = new HashMap<String,Set<Post>>();
		mentionedMessages = new HashMap<String,Set<Post>>();
		taggedMessages = new HashMap<String,Set<Post>>();
	}
}
