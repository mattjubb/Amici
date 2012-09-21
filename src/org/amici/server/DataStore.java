package org.amici.server;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import org.amici.messages.Dump;
import org.amici.messages.Post;

public interface DataStore {
	public boolean registerCertificate(String email, X509Certificate certificate);
	public X509Certificate getCertificate(String email, Date date);

	public void addPost(Post post);
	public Dump collectDump(KeyComparator comparator, Key thisNode);
	public void addDump(Dump dump);
	public Set<Post> getUserFeed(String user, long since, long until, int maxCount);
	public Set<Post> getHashTagFeed(String hashtag, long since, long until, int maxCount);
	public Set<Post> getMentionFeed(String user, long since, long until, int maxCount);
}
