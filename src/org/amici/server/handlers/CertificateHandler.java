package org.amici.server.handlers;

import java.io.Serializable;

import org.amici.Amici;
import org.amici.server.CertificateRegistration;

import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;

public class CertificateHandler implements MessageHandler{

	public static final String TAG = "C";

	public CertificateHandler(){
		Amici.getLogger( CertificateHandler.class).trace( Amici.getIdentifier() + ": Starting up");		
	}
	
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		if( content instanceof CertificateRegistration ){
			CertificateRegistration registration = (CertificateRegistration) content;
			Amici.getDataStore().registerCertificate(registration.getEmail(), registration.getCertificate());
		}
	}

	public Serializable onIncomingRequest(Node from, String tag, Serializable content) {
		return null;
	}
}
