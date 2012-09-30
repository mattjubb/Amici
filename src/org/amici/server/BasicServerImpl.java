package org.amici.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.amici.Amici;
import org.amici.messages.CertificateRegistration;
import org.amici.messages.ClientRequest;
import org.amici.messages.Dump;
import org.amici.messages.Post;
import org.amici.server.handlers.ClientHandler;
import org.amici.server.handlers.ServerHandler;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;
import il.technion.ewolf.kbr.openkad.KadNetModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BasicServerImpl implements Server {
	private KeybasedRouting router;
	private KeyFactory keyFactory;
	
	public BasicServerImpl(int basePort, URI seedNode){	
		Injector injector = Guice.createInjector(new KadNetModule()
				.setProperty("openkad.keyfactory.keysize", "3")
				.setProperty("openkad.bucket.kbuckets.maxsize", "3")
				.setProperty("openkad.executors.client.max_pending", "512")
				.setProperty("openkad.seed", ""+basePort)
				.setProperty("openkad.net.udp.port", ""+basePort));
		router = injector.getInstance(KeybasedRouting.class);

		try {
			router.create();
			if(seedNode != null) router.join(Arrays.asList(seedNode));
			keyFactory = router.getKeyFactory();
			registerHandlers();
			meetTheNeighbours();
		} catch (Exception e) {
			Amici.getLogger(BasicServerImpl.class).error( "Error initialising", e );
		}
		
		Amici.getLogger(BasicServerImpl.class).info("Amici 0.1 on " + router.getLocalNode().getInetAddress() + "/" +  router.getLocalNode().getKey() );
	}
	
	public boolean registerCertificate(String email, X509Certificate certificate){
		CertificateRegistration registration = new CertificateRegistration(email,certificate);
		if(!registration.isTrusted()){
			Amici.getLogger(BasicServerImpl.class).error( "Couldn't register: " + email );
			return false;
		}
		Key key = keyFactory.create(email);
		try {
			for(Node node:router.findNode(key))
				router.sendMessage(node, ServerHandler.TAG, registration);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public void meetTheNeighbours(){
		List<Node> neighbours = router.getNeighbours();
		if(neighbours.size()==0)
			Amici.getLogger(BasicServerImpl.class).error("I'm all alone: "+router.getLocalNode());
		else {
			Amici.getLogger(BasicServerImpl.class).info("Neighbours: "+neighbours.size());
			CompletionHandler<Serializable,Object> completionHandler = new CompletionHandler<Serializable,Object>(){
				public void completed(Serializable result, Object attachment) {
					if(result instanceof Dump)
						Amici.getDataStore().addDump((Dump) result);
				}
				public void failed(Throwable ex, Object attachment) {
					Amici.getLogger(BasicServerImpl.class).error( "Error meeting neighbours", ex );
				}
			};
			
			for(Node node:neighbours)
				router.sendRequest(node, Dump.TAG, null, null, completionHandler);
		}
	}
	
	public void registerHandlers(){
		ServerHandler serverHandler = new ServerHandler();
		router.register( ServerHandler.TAG, serverHandler);
		router.register( Dump.TAG, serverHandler);
		router.register( ClientHandler.TAG, new ClientHandler());
	}

	@Override
	public void post(Post post) throws IOException {
		for(Node node:getNodesForKeys(post.getKeys()))
			getRouter().sendMessage(node, ClientHandler.TAG, post);
	}
	
	private Set<Node> getNodesForKeys(Set<Key> keys){
		Set<Node> result = new HashSet<Node>();
		for(Key key:keys)
			result.addAll(getRouter().findNode(key));
		return result;
	}

	public KeybasedRouting getRouter() {
		return router;
	}

	public KeyFactory getKeyFactory() {
		return keyFactory;
	}
	
	public Key getKey(String data){
		return getKeyFactory().create(data);
	}

	public void sendRequest(ClientRequest request, CompletionHandler<Serializable, Object> handler) {
		Node node = getRouter().findNode(getKey(request.getParam())).get(0);
		Amici.getLogger(BasicServerImpl.class).trace("Sent request " + request.getType() +"/"+request.getParam() + " to " + node );	
		getRouter().sendRequest( node, ClientHandler.TAG, request, null, handler );
	}
}
