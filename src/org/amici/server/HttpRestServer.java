package org.amici.server;
import il.technion.ewolf.kbr.concurrent.CompletionHandler;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.amici.Amici;
import org.amici.messages.ClientRequest;
import org.amici.messages.Post;
import org.deftserver.io.IOLoop;
import org.deftserver.web.Application;
import org.deftserver.web.Asynchronous;
import org.deftserver.web.HttpServer;
import org.deftserver.web.HttpVerb;
import org.deftserver.web.handler.RequestHandler;
import org.deftserver.web.http.HttpRequest;
import org.deftserver.web.http.HttpResponse;

public class HttpRestServer implements Runnable {
	private static boolean initialised = false;
	private static int port;
	
    public static void startup( int port ) {
    	if(!initialised){
    		HttpRestServer.port = port;
    		new Thread(new HttpRestServer(), "HttpRestServer").start();
    	}
    }

	@Override
	public void run() {
		 Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
         handlers.put("/feed/user/(.*)", new HttpRestHandler());
         handlers.put("/feed/tag/(.*)", new HttpRestHandler());
         handlers.put("/feed/mentions/(.*)", new HttpRestHandler());
         handlers.put("/post", new HttpRestHandler());
         handlers.put("/register", new HttpRestHandler());
         HttpServer server = new HttpServer(new Application(handlers));
         server.listen(8080);

 		Amici.getLogger(HttpRestServer.class).trace("Starting up RestServer on " + port);	
        initialised = true;
 		while(true){
 			try{
 	 			IOLoop.INSTANCE.start();
 			}catch(Exception e){
 		 		Amici.getLogger(HttpRestServer.class).error("Exception in RestServer ",e);	
 			}
 		}
	}
	
	static class HttpRestHandler extends RequestHandler {
	    @Asynchronous
	    public void get(HttpRequest httpRequest, final HttpResponse httpResponse) {
	    	httpResponse.setHeader("Content-type","application/json");
	    	String[] args = httpRequest.getRequestedPath().split("/");
	    	
	    	CompletionHandler<Serializable,Object> completionHandler = new CompletionHandler<Serializable,Object>(){
	    		public void completed(Serializable result, Object attachment) {
			        httpResponse.write(result.toString());
			        httpResponse.finish();
	    		}
	    		public void failed(Throwable ex, Object attachment) {
	    			
	    		}
	    	};
	        
	    	if(args[1].equalsIgnoreCase("feed") && httpRequest.getMethod() == HttpVerb.GET ){
	            String param = args[3];
	    		switch(args[2].toLowerCase()){
					case "user" : {
						handleUserFeed(param,httpResponse,completionHandler);
						break;
					}
					case "tag" : {
						handleTagFeed(param,httpResponse,completionHandler);
						break;
					}
					case "mentions" : {
						handleMentionsFeed(param,httpResponse,completionHandler);
						break;
					}
	    		}
	    	}else if(args[1].equalsIgnoreCase("post") && httpRequest.getMethod() == HttpVerb.POST){
	    		try {
					Amici.getServer().post( Post.fromJson(httpRequest.getBody()) );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}else if(args[1].equalsIgnoreCase("register")){
	    		
	    	}
	    	
	    }
		public void handleUserFeed(String param, final HttpResponse httpResponse, CompletionHandler<Serializable,Object> completionHandler){
	        ClientRequest request = new ClientRequest( ClientRequest.USER, param );
	        Amici.getServer().sendRequest(request, completionHandler);
		}
		
		public void handleTagFeed(String param, final HttpResponse httpResponse, CompletionHandler<Serializable,Object> completionHandler){
	        ClientRequest request = new ClientRequest( ClientRequest.HASH_TAG, param );
	        Amici.getServer().sendRequest(request, completionHandler);
		}
		
		public void handleMentionsFeed(String param, final HttpResponse httpResponse, CompletionHandler<Serializable,Object> completionHandler){
	        ClientRequest request = new ClientRequest( ClientRequest.MENTIONS, param );
	        Amici.getServer().sendRequest(request, completionHandler);
		}
	}
}