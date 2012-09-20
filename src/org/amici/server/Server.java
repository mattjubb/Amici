package org.amici.server;

import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.messages.ClientRequest;
import org.amici.messages.Post;

public interface Server {
	public void registerCertificate(String email, X509Certificate certificate) throws IOException;
	public void postMessage(Post message) throws IOException;
	public KeybasedRouting getRouter();
	public KeyFactory getKeyFactory();
	public void sendRequest(ClientRequest request, CompletionHandler<Serializable,Object> handler);
}
