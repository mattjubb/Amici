package org.amici.messages;

import java.io.Serializable;
import java.util.Date;

public class CertificateRequest implements Serializable{
	private static final long serialVersionUID = -6578959692346977050L;
	
	public static final String TAG = "CERT_REQ";
	private String email;
	private Date date;
	
	public CertificateRequest(String email, Date date){
		this.setEmail(email);
		this.setDate(date);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String toString(){
		return email+date;
	}
}
