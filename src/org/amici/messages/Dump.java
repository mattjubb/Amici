package org.amici.messages;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Dump implements Serializable{
	private static final long serialVersionUID = -2649786550220765302L;

	public static final String TAG = "DUMP";
	
	private Set<Post> contents = new HashSet<Post>();
	public void add( Post post ){
		contents.add(post);
	}
	public void addAll( Collection<? extends Post> posts){
		contents.addAll(posts);
	}
	
	public Set<Post> getContents(){
		return contents;
	}
}
