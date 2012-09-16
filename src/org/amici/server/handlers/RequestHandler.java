package org.amici.server.handlers;

import java.io.Serializable;

import org.amici.Amici;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

public class RequestHandler implements MessageHandler{

	public static final String USER = "1";
	public static final String HASH_TAG = "2";
	public static final String MENTIONS = "3";
	
	public RequestHandler(){
		Amici.getLogger( RequestHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		return;
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		Amici.getLogger( RequestHandler.class).trace( Amici.getIdentifier() + " got " + tag + "/" + content + " from " + from.getKey());
		switch(tag.toLowerCase()){
			case USER:
				return (Serializable) Amici.getDataStore().getUserFeed(content.toString(), 10);
			case HASH_TAG:
				return (Serializable) Amici.getDataStore().getUserFeed(content.toString(), 10);
		}
		return null;
	}

}
