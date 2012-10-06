package org.amici.server.handlers;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.Amici;
import org.amici.messages.CertificateRequest;
import org.amici.messages.ClientRequest;
import org.amici.messages.Post;

public class ClientHandler implements MessageHandler{
	public static final String TAG = "CLIENT";
	public ClientHandler(){
		Amici.getLogger( ClientHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		if( content instanceof Post ){
			final Post post = (Post) content;
			X509Certificate certificate = Amici.getDataStore().getCertificate(post.getAuthor(),post.getDate());
			
			if(certificate == null){
				CertificateRequest request = new CertificateRequest(post.getAuthor(),post.getDate());
				Amici.getServer().requestCertificate(request, new CompletionHandler<Serializable, Object>(){
					public void completed(Serializable data, Object attachment) {
						X509Certificate certificate = (X509Certificate) data;
						if(post.verify(certificate)){
							Amici.getDataStore().registerCertificate(post.getAuthor(), certificate);
							Amici.getDataStore().addPost(post);
						}
					}

					public void failed(Throwable ex, Object attachment) {
						Amici.getLogger(ClientHandler.class).error(attachment);	
					}
				});
			}
			else if(post.verify(certificate))
				Amici.getDataStore().addPost(post);
		}
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		if(content instanceof ClientRequest){
			ClientRequest request = (ClientRequest) content;
			switch(request.getType()){
				case ClientRequest.USER:
					return (Serializable) Amici.getDataStore().getUserFeed(request.getParam(), request.getSince(), request.getUntil(), request.getMaxCount());
				case ClientRequest.HASH_TAG:
					return (Serializable) Amici.getDataStore().getHashTagFeed(request.getParam(), request.getSince(), request.getUntil(), request.getMaxCount());
				case ClientRequest.MENTIONS:
					return (Serializable) Amici.getDataStore().getMentionFeed(request.getParam(), request.getSince(), request.getUntil(), request.getMaxCount());
			}
		}
		return null;
	}

}
