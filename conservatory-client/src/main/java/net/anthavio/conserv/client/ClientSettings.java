package net.anthavio.conserv.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author martin.vanek
 *
 */
public class ClientSettings {

	/**
	 * Property value names
	 */
	public static final String SERVER_URL = "conserv.url";
	public static final String USERNAME = "conserv.username";
	public static final String PASSWORD = "conserv.password";
	public static final String CONNECT_TIMEOUT = "conserv.connectTimeout";
	public static final String READ_TIMEOUT = "conserv.readTimeout";
	public static final String FOLLOW_REDIRECTS = "conserv.followRedirects";
	public static final String CONFIG_MEMORY_CACHING = "conserv.configMemoryCaching";
	public static final String CONFIG_FILE_CACHING = "conserv.configFileCaching";
	public static final String CONFIG_FILE_DIRECTORY = "conserv.configFileDirectory";

	private static final Logger logger = LoggerFactory.getLogger(ClientSettings.class);

	private final URL serverUrl;

	private String username; //BASIC authentication credentials 

	private String password;

	private int connectTimeout = 2000;

	private int readTimeout = 5000;

	private boolean followRedirects = true; // 300 response behaviour

	private ConfigParser configParser = ConfigParserFactory.build();

	private ConservLoader conservLoader = new ConservUrlLoader();

	//keep last server loaded version in memory
	private boolean configMemoryCaching = true;

	//keep last server loaded version in file
	private boolean configFileCaching = true;

	//where to keep configuration files
	private String configDirectory = System.getProperty("java.io.tmpdir");

	/**
	 * Initialization from configuration properties
	 */
	public ClientSettings(Properties properties) {
		String serverUrl = properties.getProperty(SERVER_URL);
		if (serverUrl != null) {
			this.serverUrl = ConservResource.parseURL(serverUrl);
		} else {
			String configUrl = properties.getProperty(ConservResource.CONFIG_URL);
			if (configUrl != null) {
				String[] elements = ConservResource.parseConfigUrl(configUrl);
				this.serverUrl = ConservResource.parseURL(elements[0]);
			} else {
				throw new ConservInitException("Neither '" + SERVER_URL + "' nor '" + ConservResource.CONFIG_URL
						+ "' property found");
			}
		}

		this.username = properties.getProperty(USERNAME);
		this.password = properties.getProperty(PASSWORD);

		this.connectTimeout = getOptInt(properties, CONNECT_TIMEOUT, connectTimeout);
		this.readTimeout = getOptInt(properties, READ_TIMEOUT, connectTimeout);
		this.followRedirects = getOptBoolean(properties, FOLLOW_REDIRECTS, followRedirects);

		this.configMemoryCaching = getOptBoolean(properties, CONFIG_MEMORY_CACHING, configMemoryCaching);
		this.configFileCaching = getOptBoolean(properties, CONFIG_FILE_CACHING, configFileCaching);
		this.configDirectory = properties.getProperty(CONFIG_FILE_DIRECTORY, configDirectory);
	}

	public ClientSettings(String serverUrl) {
		try {
			this.serverUrl = new URL(serverUrl);
		} catch (MalformedURLException mux) {
			throw new IllegalArgumentException("Malformed url: " + serverUrl, mux);
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

	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
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

	public boolean getConfigFileCaching() {
		return configFileCaching;
	}

	public void setConfigKeeping(boolean configKeeping) {
		this.configFileCaching = configKeeping;
	}

	public boolean getConfigMemoryCaching() {
		return configMemoryCaching;
	}

	public void setConfigMemoryCaching(boolean configMemoryCaching) {
		this.configMemoryCaching = configMemoryCaching;
	}

	public ConfigParser getConfigParser() {
		return configParser;
	}

	public void setConfigParser(ConfigParser configParser) {
		this.configParser = configParser;
	}

	public ConservLoader getConservLoader() {
		return conservLoader;
	}

	public void setConservLoader(ConservLoader conservLoader) {
		this.conservLoader = conservLoader;
	}

	public static String getRequired(Properties properties, String name) {
		String value = properties.getProperty(name);
		if (value == null) {
			throw new ConservInitException("Required property " + name + " not found");
		} else if (value.length() == 0) {
			throw new ConservInitException("Required property " + name + " is blank");
		} else {
			return value;
		}
	}

	public static int getOptInt(Properties properties, String name, int defval) {
		String string = properties.getProperty(name);
		if (string != null && string.length() != 0) {
			try {
				return Integer.parseInt(string);
			} catch (Exception x) {
				logger.warn("Invalid value for int '" + string + "' Keepind default value:" + defval);
				return defval;
			}
		} else {
			return defval;
		}
	}

	public static boolean getOptBoolean(Properties proprties, String name, boolean defval) {
		String string = proprties.getProperty(name);
		if (string != null && string.length() != 0) {
			return Boolean.parseBoolean(string);
		} else {
			return defval;
		}
	}

	@Override
	public String toString() {
		return "ClientSettings [serverUrl=" + serverUrl + ", username=" + username + ", password=" + /*password*/"******"
				+ ", followRedirects=" + followRedirects + ", connectTimeout=" + connectTimeout + ", readTimeout="
				+ readTimeout + ", configKeeping=" + configFileCaching + ", configDirectory=" + configDirectory + "]";
	}

}
