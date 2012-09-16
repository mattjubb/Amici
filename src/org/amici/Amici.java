package org.amici;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.amici.server.DataStore;
import org.amici.server.BasicDataStoreImpl;
import org.amici.server.FutureWorker;
import org.amici.server.Server;
import org.amici.server.BasicServerImpl;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Amici {

	private static DataStore dataStore = null;
	private static Server server = null;
	private static boolean initialised = false;
	private static Logger logger = Logger.getRootLogger();
	
	public static String HOST_IDENTIFIER = "AmiciBeta";
	
	public static void main(String[] args) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, URISyntaxException {
		URI peer = args.length == 2 ? new URI(args[1]) : null;
		startup(Integer.parseInt(args[0]), peer);
	}
	
	public static synchronized void startup( int port, URI peer ){
		if( !initialised ){
			getLogger().setLevel(Level.ALL);
			server = new BasicServerImpl( port, peer );
			dataStore = new BasicDataStoreImpl();
			FutureWorker.startup();
			initialised = true;
		}
	}
	
	public static String getIdentifier(){
		try{
			return server.getRouter().getLocalNode().getKey().toBase64();
		}catch(Exception e){
			return Amici.HOST_IDENTIFIER + "(booting)";
		}
	}
	
	public static DataStore getDataStore(){
		return dataStore;
	}

	public static Server getServer(){
		return server;
	}
	
	public static Logger getLogger(){
		return logger;
	}

	public static Logger getLogger(Class clazz){
		return Logger.getLogger(clazz);
	}
}
