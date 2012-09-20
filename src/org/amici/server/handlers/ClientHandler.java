package org.amici.server.handlers;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.Amici;
import org.amici.messages.ClientRequest;
import org.amici.messages.Post;

public class ClientHandler implements MessageHandler{
	public static final String TAG = "CLIENT";
	public ClientHandler(){
		Amici.getLogger( ClientHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		if( content instanceof Post ){
			Post message = (Post) content;
			X509Certificate certificate = Amici.getDataStore().getCertificate(message.getAuthor());
			if(message.verify(certificate))
				Amici.getDataStore().addMessage(message);
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
