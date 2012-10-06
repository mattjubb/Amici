package org.amici;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.amici.messages.Post;
import org.amici.server.HttpRestServer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class RestServer {
	
	private static GsonBuilder gson_builder = new GsonBuilder();
	
	static {
			gson_builder.registerTypeAdapter(
	        JsonElement.class,
	        new JsonDeserializer<JsonElement>() {
				public JsonElement deserialize(JsonElement json, Type typeOfT,JsonDeserializationContext context) throws JsonParseException {
	                return json;
				}
	        } );
	}
	
	@After
	public void clear(){
		Amici.clearDataStore();
	}
	
	@Test
	public void registerCertificate(){
		try {
			String email = "test@example.com";	
			X509Certificate cert = CertificateUtils.getTestCertificate(email);
			String result = TestUtils.doPost(new URL("http://localhost:8080/register/" +email),CertificateUtils.certificateToString(cert));
			assertEquals("{ status: 'OK' }",result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void post(){
		try {
			String email = "test@example.com";
			KeyPair keyPair = CertificateUtils.getKeyPair();
			X509Certificate cert = CertificateUtils.getTestCertificate(keyPair, email);
			Post post = new Post(email,"hello there #wassup ~bob@example.com");
			TestUtils.doPost(new URL("http://localhost:8080/register/" +email),CertificateUtils.certificateToString(cert));
			post.sign(keyPair.getPrivate());
			String result = TestUtils.doPost(new URL("http://localhost:8080/post"),post.toString());
			assertEquals("{ status: 'OK' }",result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void retrieve(){
		try {
			String email = "test@example.com";
			KeyPair keyPair = CertificateUtils.getKeyPair();
			X509Certificate cert = CertificateUtils.getTestCertificate(keyPair, email);
			Post post = new Post(email,"hello there #wassup ~bob@example.com");

			post.addHostingDetails(Amici.getServer().getRouter().getLocalNode(), Amici.HOST_IDENTIFIER);
			assertEquals("{ status: 'OK' }",TestUtils.doPost(new URL("http://localhost:8080/register/" +email),CertificateUtils.certificateToString(cert)));
			post.sign(keyPair.getPrivate());
			TestUtils.doPost(new URL("http://localhost:8080/post"),post.toString());
			
			String result = TestUtils.doGet(new URL("http://localhost:8080/feed/user/"+email));
			Gson gson = gson_builder.create();
			JsonObject obj = gson.fromJson(result,JsonElement.class ).getAsJsonObject();
			Post postBack = gson.fromJson(obj.get("data").getAsJsonArray().get(0), Post.class);
			assertEquals(post,postBack);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@BeforeClass
	public static void setupServers() {
		System.setProperty("amici_testing", "true");
		Amici.startup( 5555,null );
		HttpRestServer.startup(8080);
	}

}
