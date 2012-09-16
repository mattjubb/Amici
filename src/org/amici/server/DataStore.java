package org.amici.server;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyComparator;

import java.security.cert.X509Certificate;
import java.util.Set;

import org.amici.Message;

public interface DataStore {
	public boolean registerCertificate(String email, X509Certificate certificate);
	public X509Certificate getCertificate(String email);

	public void addMessage(Message message);
	public Set<Message> collectCloseMessages(KeyComparator comparator, Key thisNode);
	public Set<Message> getUserFeed(String user, int count);
	public Set<Message> getHashTagFeed(String hashtag, int count);
	public Set<Message> getMentionFeed(String hashtag, int count);
}
