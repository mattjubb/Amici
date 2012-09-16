package org.amici.server.handlers;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.Amici;
import org.amici.Message;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;


public class MsgHandler implements MessageHandler{
	public MsgHandler(){
		Amici.getLogger( MsgHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		Message message = (Message) content;
		X509Certificate certificate = Amici.getDataStore().getCertificate(message.getAuthor());
		if(message.verify(certificate))
			Amici.getDataStore().addMessage(message);
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		return null;
	}

}
