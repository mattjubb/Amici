package org.amici.server.handlers;

import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.Amici;
import org.amici.messages.CertificateRegistration;
import org.amici.messages.CertificateRequest;
import org.amici.messages.Dump;

public class ServerHandler implements MessageHandler{

	public static final String TAG = "SERVER";
	
	public ServerHandler(){
		Amici.getLogger( ServerHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		if( content instanceof CertificateRegistration ){
			CertificateRegistration registration = (CertificateRegistration) content;
			Amici.getDataStore().registerCertificate(registration.getEmail(), registration.getCertificate());
		}
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		if (tag.equalsIgnoreCase(Dump.TAG)){
			KeyComparator comparator = new KeyComparator( from.getKey() );
			return Amici.getDataStore().collectDump(comparator, Amici.getServer().getRouter().getLocalNode().getKey());
		} else if(tag.equalsIgnoreCase(CertificateRequest.TAG)){
			CertificateRequest request = (CertificateRequest) content;
			return Amici.getDataStore().getCertificate(request.getEmail(),request.getDate());
		}
		return null;
	}

}
