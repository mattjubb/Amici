package org.amici.server.handlers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.amici.Amici;
import org.amici.Message;

import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

public class InfraHandler implements MessageHandler{

	public static final String TAG = "S";

	public InfraHandler(){
		Amici.getLogger( InfraHandler.class).trace( Amici.getIdentifier() + ": Starting up");
	}
	
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		try {
			KeyComparator comparator = new KeyComparator( from.getKey() );
			Set<Message> dump = Amici.getDataStore().collectCloseMessages(comparator, Amici.getServer().getRouter().getLocalNode().getKey());
			Amici.getLogger( InfraHandler.class).trace( "Request from " + from + ", size: " + dump.size());
			Iterator<Message> messIterator = dump.iterator();
			while(messIterator.hasNext())
				Amici.getServer().postMessage(messIterator.next());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		return null;
	}


}
