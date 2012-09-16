package org.amici.server;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FutureWorker {
	private static ScheduledExecutorService executor;
	private static boolean initialised = false;
	private static List<ListeningFuture<Serializable>> listeners = new LinkedList<ListeningFuture<Serializable>>();
	
	public static synchronized void startup(){
		if(!initialised){
			executor = Executors.newSingleThreadScheduledExecutor();
			Runnable command = new Runnable(){
				public void run() {
					for(ListeningFuture<Serializable> future:FutureWorker.getListeners()){
						if(future.getFuture().isDone()){
							if(!future.getFuture().isCancelled()){
								try {
									future.handleResult(future.getFuture().get());
								} catch (InterruptedException | ExecutionException e) {
									e.printStackTrace();
								}
							}
							FutureWorker.getListeners().remove(future);
						}
					}
				}
			};
			executor.scheduleWithFixedDelay(command, 500, 500, TimeUnit.MILLISECONDS);
			initialised = true;
		}
	}
	
	public static List<ListeningFuture<Serializable>> getListeners(){
		return listeners;
	}
	public static void add( ListeningFuture<Serializable> future ){
		getListeners().add(future);
	}
}