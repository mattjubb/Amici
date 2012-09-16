package org.amici.utils;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

public class CertificateUtils {
	public static boolean initialised = false;
	
	public static KeyStore getKeyStore( File file, String password ){
		initialise();
        FileInputStream fis = null;
        KeyStore store = null;
        try {
    		store = KeyStore.getInstance("BCPKCS12", "BC");
            fis = new FileInputStream(file);
            store.load(fis, password.toCharArray());
        } catch (Exception e) {
			e.printStackTrace();
		} 
	    return store;
	}
	
	public static String signData(String data, PrivateKey key){
		initialise();
		String result = "";
		try{
			Signature sig = Signature.getInstance("RIPEMD160WithRSA/ISO9796-2", "BC");
	        sig.initSign(key);
	        sig.update(data.getBytes());
	        byte[] sigBytes = Base64.encodeBase64(sig.sign());
	        result = new String(sigBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return result;
	}
	
	public static boolean checkSignature(String data, String email, String signature, X509Certificate certificate){
		initialise();
		boolean result = false;
		try{
			if( !isTrusted(certificate,email) ) return false;
			
			Signature sig = Signature.getInstance("RIPEMD160WithRSA/ISO9796-2", "BC");
			sig.initVerify(certificate.getPublicKey());
	
	        sig.update(data.getBytes());
	        byte[] sigBytes = Base64.decodeBase64(signature.getBytes());
	        result = sig.verify(sigBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static boolean isTrusted(X509Certificate certificate, String email){
		initialise();
		try{
			X500Name subject = new JcaX509CertificateHolder(certificate).getSubject();
			RDN[] ERDNs =  subject.getRDNs(BCStyle.E);
			
			String certEmail = "";
			
			if( ERDNs.length > 0 ){
				certEmail = IETFUtils.valueToString( ERDNs[0].getFirst().getValue() );
			}else{
				RDN[] EmailRDNs =  subject.getRDNs(BCStyle.EmailAddress);
				if( EmailRDNs.length > 0 )
					certEmail = IETFUtils.valueToString( EmailRDNs[0].getFirst().getValue() );
				else return false;
			}
				
			if(!certEmail.equalsIgnoreCase(email))
				return false;
			
			KeyStore keystore = KeyStore.getInstance("KeychainStore", "Apple");
        	keystore.load(null);
            // This class retrieves the most-trusted CAs from the keystore
            PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            Iterator<TrustAnchor> it = params.getTrustAnchors().iterator();
            
        	while( it.hasNext() ) {
                try{
                    // Get certificate
                    X509Certificate caCertificate = it.next().getTrustedCert();
                    certificate.verify(caCertificate.getPublicKey());
    				return true;
                }catch(Exception e) {
    				continue;
				}
            }
		} catch (Exception e1) {
			return false;
		}
		return false;
	}
	
	private synchronized static void initialise(){
		if(!initialised){
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			initialised = true;
		}
	}
}
