package org.amici;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.x509.X509V1CertificateGenerator;

public class CertificateUtils {
	public static boolean initialised = false;
	private static Map<X509Certificate,Boolean> trustedEmails;
	
	public static KeyStore getKeyStore( File file, String password ){
		initialise();
        FileInputStream fis = null;
        KeyStore store = null;
        try {
    		store = KeyStore.getInstance("BCPKCS12", "BC");
            fis = new FileInputStream(file);
            store.load(fis, password.toCharArray());
        } catch (Exception e) {
			Amici.getLogger(CertificateUtils.class).error( "Error getting keystore", e );
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
			Amici.getLogger(CertificateUtils.class).error( "Error signing data", e );
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
			Amici.getLogger(CertificateUtils.class).error( "Error signing data", e );
		}
		return result;
	}
	
	public static String certificateToString(X509Certificate certificate){
		try {
			return new String(Base64.encodeBase64(certificate.getEncoded()));
		} catch (CertificateEncodingException e) {
			return "Error";
		}
	}

	public static X509Certificate stringToCertificate(String certString){
		InputStream in = null;  
        X509Certificate cert = null;  
        try {  
            byte[] certEntryBytes = Base64.decodeBase64(certString.getBytes());  
            in = new ByteArrayInputStream(certEntryBytes);  
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");  
  
            cert = (X509Certificate) certFactory.generateCertificate(in);  
        } catch (CertificateException ex) {  
        	return null;
        } 
        return cert; 
	}
	
	public static boolean isTrusted(X509Certificate certificate, String email){
		initialise();
		
		
		String certEmail = "Unknown";
		
		if(trustedEmails.containsKey(certificate))
			return(trustedEmails.get(certificate));
		
		try{
			X500Name subject = new JcaX509CertificateHolder(certificate).getSubject();
			RDN[] ERDNs =  subject.getRDNs(BCStyle.E);
						
			if( ERDNs.length > 0 ){
				certEmail = IETFUtils.valueToString( ERDNs[0].getFirst().getValue() );
			}else{
				RDN[] EmailRDNs =  subject.getRDNs(BCStyle.EmailAddress);
				if( EmailRDNs.length > 0 )
					certEmail = IETFUtils.valueToString( EmailRDNs[0].getFirst().getValue() );
				else {
					trustedEmails.put(certificate, false);
					Amici.getLogger(CertificateUtils.class).error( certEmail +  " is NOT Trusted");
					return false;
				}
			}
				
			if(!certEmail.equalsIgnoreCase(email)){
				trustedEmails.put(certificate, false);
				Amici.getLogger(CertificateUtils.class).error( certEmail +  " is NOT Trusted");
				return false;
			}
			
			if( Amici.isTestingMode() ) {
				trustedEmails.put(certificate, true);
				return true;
			}
			
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
            		Amici.getLogger(CertificateUtils.class).trace( certEmail +  " is trusted by " + caCertificate.getIssuerDN());
            		trustedEmails.put(certificate, true);
    				return true;
                }catch(Exception e) {
    				continue;
				}
            }
		} catch (Exception e1) {
    		trustedEmails.put(certificate, false);
    		Amici.getLogger(CertificateUtils.class).error( certEmail +  " is NOT Trusted");
			return false;
		}
		trustedEmails.put(certificate, false);
		Amici.getLogger(CertificateUtils.class).error( certEmail +  " is NOT Trusted");
		return false;
	}
	
	public static KeyPair getKeyPair(){
		try{
		    // GENERATE THE PUBLIC/PRIVATE RSA KEY PAIR
		    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
		    keyPairGenerator.initialize(1024, new SecureRandom());
		    return keyPairGenerator.generateKeyPair();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static X509Certificate getTestCertificate( KeyPair keyPair, String email ){
		try{
			Date validityBeginDate = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
			    // in 2 years
			Date validityEndDate = new Date(System.currentTimeMillis() + ( 1000 * 24 * 60 * 60 * 1000));
		    // GENERATE THE X509 CERTIFICATE
		    X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		    X500Principal dnName = new X500Principal("CN=John Doe");
		    X500Principal eName = new X500Principal("EMAILADDRESS=" + email);

		    certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		    certGen.setSubjectDN(eName);
		    certGen.setIssuerDN(dnName); // use the same
		    certGen.setNotBefore(validityBeginDate);
		    certGen.setNotAfter(validityEndDate);
		    certGen.setPublicKey(keyPair.getPublic());
		    certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		    return certGen.generate(keyPair.getPrivate(), "BC");
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static X509Certificate getTestCertificate(String email ){
		return getTestCertificate( getKeyPair(), email );
	}
	
	private synchronized static void initialise(){
		if(!initialised){
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			trustedEmails = new HashMap<X509Certificate,Boolean>();
			initialised = true;
			Amici.getLogger( CertificateUtils.class).info( "CertificateUtils initialised OK");	
		}
	}
}
