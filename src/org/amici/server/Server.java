package org.amici.server;

import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.amici.Message;
import org.amici.client.handlers.ClientRequestHandler;

public interface Server {
	public void registerCertificate(String email, X509Certificate certificate) throws IOException;
	public void postMessage(Message message) throws IOException;
	public KeybasedRouting getRouter();
	public KeyFactory getKeyFactory();
	public void getUserFeed(String user, final ClientRequestHandler handler);
}
