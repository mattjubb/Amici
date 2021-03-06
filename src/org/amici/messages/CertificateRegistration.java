package org.amici.messages;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.amici.CertificateUtils;

import com.google.gson.Gson;

public class CertificateRegistration implements Serializable{
	private static final long serialVersionUID = 7286368834000815041L;
	private String email;
	private X509Certificate certificate;

	private static Gson gson = new Gson();
	
	public CertificateRegistration(String email, X509Certificate certificate){
		this.email = email;
		this.certificate = certificate;
	}
	
	public String getEmail(){
		return email;
	}
	
	public X509Certificate getCertificate(){
		return certificate;
	}
	
	public boolean isTrusted(){
		return CertificateUtils.isTrusted(certificate, email);
	}

	public String toString(){
		return gson.toJson(this);
	}
}
