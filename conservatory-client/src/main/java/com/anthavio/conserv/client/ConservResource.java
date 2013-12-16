package com.anthavio.conserv.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.anthavio.conserv.model.Config;

/**
 * 
 * @author martin.vanek
 *
 */
public class ConservResource {

	/**
	 * @return ConservResource discovered with default location techniques and parameters
	 * @throws ConservInitException when default configuration cannot be located  
	 */
	public static ConservResource Default() throws ConservInitException {
		Properties properties = ConservClient.Discover();
		ClientSettings settings = new ClientSettings(properties);
		ConservClient client = new ConservClient(settings);
		ConservResource resource = new ConservResource(client, properties);
		return resource;
	}

	/**
	 * String format - http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 */
	public static final String CONFIG_URL = "conserv.config.url";

	/**
	 * String format - {environment}/{application}/{resource}
	 */
	public static final String CONFIG_PATH = "conserv.config.path";

	public static final String CONFIG_ENVIRONMENT = "conserv.config.environment";
	public static final String CONFIG_APPLICATION = "conserv.config.application";
	public static final String CONFIG_RESOURCE = "conserv.config.resource";

	private final ConservClient client;

	private final String environment;

	private final String application;

	private final String resource;

	/**
	 * Creates own ConfigClient instance
	 * 
	 * @param configUrl - http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 */
	public ConservResource(String configUrl) {
		this(parseURL(configUrl));
	}

	/**
	 * Creates own ConfigClient instance
	 * 
	 * @param configUrl - http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 */
	public ConservResource(URL configUrl) {
		String resourcePath = configUrl.getPath();
		String[] coordinates = parseConfigPath(resourcePath);
		environment = coordinates[0];
		application = coordinates[1];
		resource = coordinates[2];
		/*
		String[] parts = resourcePath.split("/");
		if (parts.length >= 3) { //get last 3 elements from path
			environment = parts[parts.length - 3];
			application = parts[parts.length - 2];
			resource = parts[parts.length - 1];
		} else {
			throw new ConservInitException("Missing configuration elements in url: " + configUrl);
		}
		*/
		//Remove config path from url and initialize ConservClient with it
		String serverUrl = configUrl.toString().substring(0,
				resourcePath.indexOf("/" + environment + "/" + application + "/" + resource));
		this.client = new ConservClient(serverUrl);
	}

	/**
	 * Creates own ConfigClient instance
	 * 
	 * @param serverUrl - http://config.server.com:8080/conserv
	 * @param configPath {environment}/{application}/{resource}
	 */
	public ConservResource(String serverUrl, String configPath) {
		this(new ConservClient(serverUrl), configPath);
	}

	/**
	 * Creates own ConfigClient instance
	 * 
	 * @param serverUrl - http://config.server.com:8080/conserv
	 * @param environment
	 * @param application
	 * @param resource
	 */
	public ConservResource(String serverUrl, String environment, String application, String resource) {
		this(new ConservClient(serverUrl), environment, application, resource);
	}

	/**
	 * @param client - provided initialized ConservClient
	 * @param resourcePath {environment}/{application}/resource
	 */
	public ConservResource(ConservClient client, String configPath) {
		this(client, parseConfigPath(configPath));
	}

	private ConservResource(ConservClient client, String[] coordinates) {
		this(client, coordinates[0], coordinates[1], coordinates[2]);
	}

	/**
	 * @param client - provided initialized ConservClient
	 * @param environment
	 * @param application
	 * @param resource
	 */
	public ConservResource(ConservClient client, String environment, String application, String resource) {
		if (client == null) {
			throw new IllegalArgumentException("Null client");
		}
		this.client = client;
		this.environment = environment;
		this.application = application;
		this.resource = resource;
		String resourcePath = environment + "/" + application + "/" + resource;
		String serverPath = client.getServerUrl().getPath();
		if (serverPath.indexOf(resourcePath) != -1) {
			throw new IllegalArgumentException("Server url " + client.getServerUrl() + " contains resource path "
					+ resourcePath);
		}
	}

	public ConservResource(Properties properties) {
		this(new ConservClient(new ClientSettings(properties)), properties);
	}

	public ConservResource(ConservClient client, Properties properties) {
		if (client == null) {
			throw new IllegalArgumentException("Null client");
		}
		this.client = client;
		String configPath = properties.getProperty(CONFIG_PATH);
		if (configPath != null) {
			String[] coordinates = parseConfigPath(configPath);
			this.environment = coordinates[0];
			this.application = coordinates[1];
			this.resource = coordinates[2];
		} else {
			this.environment = getRequired(properties, CONFIG_ENVIRONMENT);
			this.application = getRequired(properties, CONFIG_APPLICATION);
			this.resource = getRequired(properties, CONFIG_RESOURCE);
		}

	}

	public ConservClient getClient() {
		return client;
	}

	public Config get() {
		return this.client.getConfig(environment, application, resource);
	}

	private static URL parseURL(String string) {
		try {
			return new URL(string);
		} catch (MalformedURLException mux) {
			throw new IllegalArgumentException("Invalid url " + string, mux);
		}
	}

	public static String[] parseConfigPath(String path) {
		int sidx = 0;
		int eidx = path.length() - 1;
		if (path.startsWith("/")) {
			sidx = 1;
		}
		if (path.endsWith("/")) {
			eidx = path.length() - 1;
		}
		if (sidx > 0 || eidx < path.length()) {
			path = path.substring(sidx, eidx);
		}
		String[] parts = path.split("/");
		if (parts.length < 3) { //get last 3 elements from path
			throw new ConservInitException("Missing configuration elements in " + path);
		} else {
			String[] retval = new String[3];
			retval[0] = parts[parts.length - 3];
			if (retval[0].length() == 0) {
				throw new ConservInitException("Environment coordinate is blank in " + path);
			}
			retval[1] = parts[parts.length - 2];
			if (retval[0].length() == 0) {
				throw new ConservInitException("Application coordinate is blank in " + path);
			}
			retval[2] = parts[parts.length - 1];
			if (retval[0].length() == 0) {
				throw new ConservInitException("Resource coordinate is blank in " + path);
			}
			System.out.println(retval[0] + " " + retval[1] + " " + retval[2]);
			return retval;
		}
	}

	private static String getRequired(Properties properties, String name) {
		String value = properties.getProperty(name);
		if (value == null) {
			throw new ConservInitException("Required property " + name + " not found");
		} else if (value.length() == 0) {
			throw new ConservInitException("Required property " + name + " is blank");
		} else {
			return value;
		}
	}
}
