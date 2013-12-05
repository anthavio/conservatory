package com.anthavio.conserv.client;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author martin.vanek
 *
 */
public class ClientSettings {

	private URL serverUrl;

	private String username; //BASIC authentication credentials 

	private String password;

	private boolean followRedirects = true; // 300 response behaviour

	private int connectTimeout = 2000;

	private int readTimeout = 5000;

	private ConfigParser configParser;

	//keep last server loaded version locally
	private boolean configKeeping = true;

	//where to keep configuration files
	private String configDirectory = System.getProperty("java.io.tmpdir");

	public ClientSettings(String serverUrl) {
		try {
			this.serverUrl = new URL(serverUrl);
		} catch (MalformedURLException mux) {
			throw new IllegalArgumentException("Malformed url " + serverUrl, mux);
		}
	}

	public ClientSettings(URL serverUrl) {
		if (serverUrl == null) {
			throw new IllegalArgumentException("Null url");
		}
		this.serverUrl = serverUrl;
	}

	public URL getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(URL serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean getFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getConfigDirectory() {
		return configDirectory;
	}

	public void setConfigDirectory(String configDirectory) {
		this.configDirectory = configDirectory;
	}

	public boolean getConfigKeeping() {
		return configKeeping;
	}

	public void setConfigKeeping(boolean configKeeping) {
		this.configKeeping = configKeeping;
	}

	public ConfigParser getConfigParser() {
		return configParser;
	}

	public void setConfigParser(ConfigParser configParser) {
		this.configParser = configParser;
	}

	@Override
	public String toString() {
		return "ClientSettings [serverUrl=" + serverUrl + ", username=" + username + ", password=" + /*password*/"******"
				+ ", followRedirects=" + followRedirects + ", connectTimeout=" + connectTimeout + ", readTimeout="
				+ readTimeout + ", configKeeping=" + configKeeping + ", configDirectory=" + configDirectory + "]";
	}

}
