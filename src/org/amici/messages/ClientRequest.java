package org.amici.messages;

import java.io.Serializable;

import com.google.gson.Gson;

public class ClientRequest implements Serializable{
	
	public static final String USER = "user";
	public static final String HASH_TAG = "hashtag";
	public static final String MENTIONS = "mentions";
		
	private static final long serialVersionUID = 8111707139078536253L;

	private static Gson gson = new Gson();
	
	private String type;
	private String param;
	private long since = 0;
	private long until = Long.MAX_VALUE;
	private int maxCount = 10;

	public ClientRequest(String type, String param){
		this.type = type;
		this.param = param;
	}
	public ClientRequest(String type, String param,long until){
		this(type,param);
		this.until=until;
	}
	public ClientRequest(String type, String param,long since,long until){
		this(type,param,until);
		this.since = since;
	}
	public ClientRequest(String type, String param,long since,long until, int maxCount){
		this(type,param,since,until);
		this.maxCount = maxCount;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public long getSince() {
		return since;
	}
	public void setSince(long since) {
		this.since = since;
	}
	public long getUntil() {
		return until;
	}
	public void getUntil(long until) {
		this.until = until;
	}
	public int getMaxCount() {
		return maxCount;
	}
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public String toString(){
		return gson.toJson(this);
	}
	
}
