package org.amici;

import static org.junit.Assert.*;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import org.junit.Test;

public class CertificateTests {

	@Test
	public void testCertificate() throws KeyStoreException {
		X509Certificate cert = CertificateUtils.getTestCertificate("test@example.com");
		assertNotNull("Test certificate not generated", cert);
	}
	

	@Test
	public void testStringAndBack() throws KeyStoreException {
		X509Certificate cert = CertificateUtils.getTestCertificate("test@example.com");
		String certStr = CertificateUtils.certificateToString(cert);
		assertNotNull("Couldn't turn Certificate to String", certStr);
		
		X509Certificate certBack =  CertificateUtils.stringToCertificate(certStr);

		assertNotNull("Couldn't turn String back to Certificate", certBack);
		
		assertEquals(cert,certBack);
	}

}
