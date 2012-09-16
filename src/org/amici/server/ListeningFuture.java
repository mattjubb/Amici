package org.amici.server;

import java.util.concurrent.Future;

public interface ListeningFuture<T>{
	public Future<T> getFuture();
	public void handleResult( T result);
}
