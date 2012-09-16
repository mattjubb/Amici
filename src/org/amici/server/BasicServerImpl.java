package org.amici.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.amici.Amici;
import org.amici.Message;
import org.amici.client.handlers.ClientRequestHandler;
import org.amici.server.handlers.CertificateHandler;
import org.amici.server.handlers.InfraHandler;
import org.amici.server.handlers.MsgHandler;
import org.amici.server.handlers.RequestHandler;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeyFactory;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.KadNetModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BasicServerImpl implements Server {
	private KeybasedRouting router;
	private KeyFactory keyFactory;
	private Executor executor;
	
	public BasicServerImpl(int basePort, URI seedNode){	
		int cores = Runtime.getRuntime().availableProcessors();
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(1000, false);
		this.executor = new ThreadPoolExecutor( cores, cores, 1, TimeUnit.MINUTES,workQueue);

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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Amici.getLogger().info("Amici 0.1 on " + router.getLocalNode().getInetAddress() + "/" +  router.getLocalNode().getKey() );
	}
	
	public void registerCertificate(String email, X509Certificate certificate) throws IOException{
		CertificateRegistration registration = new CertificateRegistration(email,certificate);
		if(!registration.isTrusted())
			throw new RuntimeException();
		Key key = keyFactory.create(email);
		for(Node node:router.findNode(key))
			router.sendMessage(node, CertificateHandler.TAG, registration);
	}
	
	public void meetTheNeighbours(){
		List<Node> neighbours = router.getNeighbours();
		if(neighbours.size()==0)
			System.err.println("I'm all alone: "+router.getLocalNode());
		else System.err.println("Neighbours: "+neighbours.size());
		try {
			for(Node node:neighbours){
				router.sendMessage(node, InfraHandler.TAG, null );
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerHandlers(){
		router.register( CertificateHandler.TAG, new CertificateHandler() );
		MsgHandler msgHandler = new MsgHandler();
		RequestHandler requestHandler = new RequestHandler();
		router.register( Message.AUTHOR_TAG, msgHandler);
		router.register( Message.HASH_TAG, msgHandler);
		router.register( Message.MENTION_TAG, msgHandler);
		router.register( RequestHandler.USER, requestHandler);
		router.register( InfraHandler.TAG, new InfraHandler());
	}

	@Override
	public void postMessage(Message message) throws IOException {
		Key authorKey = keyFactory.create(message.getAuthor());

		for(Node node:router.findNode(authorKey)){
			Amici.getLogger( BasicServerImpl.class).trace( message.getBase64Hash() + " by authored stored at " + node );	
			router.sendMessage(node, Message.AUTHOR_TAG, message);	
		}
		
		for(String tag:message.getTags()){
			Key tagKey = keyFactory.create(tag);
			for(Node node:router.findNode(tagKey)){
				Amici.getLogger( BasicServerImpl.class).trace( message.getBase64Hash() + " by tag stored at " + node );	
				router.sendMessage(node, Message.HASH_TAG, message);
			}
		}
			
		for(String mention:message.getMentions()){
			Key mentionKey = keyFactory.create(mention);
			for(Node node:router.findNode(mentionKey)){
				Amici.getLogger( BasicServerImpl.class).trace( message.getBase64Hash() + " by mention stored at " + node );	
				router.sendMessage(node, Message.MENTION_TAG, message);
			}
		}
	}

	@Override
	public KeybasedRouting getRouter() {
		return router;
	}

	@Override
	public KeyFactory getKeyFactory() {
		return keyFactory;
	}
	
	public Key getKey(String data){
		return getKeyFactory().create(data);
	}
	
	public void getUserFeed(String user, ClientRequestHandler handler){
		sendRequest( RequestHandler.USER, user, handler );
	}
	
	public void getHashTagFeed(String tag, ClientRequestHandler handler){
		sendRequest( RequestHandler.HASH_TAG, tag, handler );
	}
	
	public void getMentionsFeed(String user, ClientRequestHandler handler){
		sendRequest( RequestHandler.MENTIONS, user, handler );
	}
	
	public void sendRequest(String tag, String param, final ClientRequestHandler handler){
		Node node = getRouter().findNode(getKey(param)).get(0);
		Amici.getLogger( BasicServerImpl.class).trace( "Sent request " + tag + "/" + param + " to " + node );	
		final Future<Serializable> result = getRouter().sendRequest(node, tag, param);
		FutureWorker.add(new ListeningFuture<Serializable>(){
			public void handleResult(Serializable result) {
				handler.handlerResult(result);
			}
			public Future<Serializable> getFuture() {
				return result;
			}
		});
	}
}
