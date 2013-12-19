package net.anthavio.conserv.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import net.anthavio.conserv.model.Config;
import net.anthavio.discovery.PropertiesDiscovery;
import net.anthavio.discovery.PropertiesDiscovery.Result;


/**
 * 
 * @author martin.vanek
 *
 */
public class ConservResource {

	/**
	 * @return ConservResource found with default discovery machanism 
	 * @throws ConservInitException when default configuration cannot be located  
	 */
	public static ConservResource Default() throws ConservInitException {
		Result<?> result = ConservClient.DefaultDiscovery();
		Properties properties = result.getProperties();
		ConservResource resource = new ConservResource(properties);

		//TODO we can append result.getSource() to message to simplify navidation to properties source
		return resource;
	}

	/**
	 * (System) property name of the complete configuration url 
	 * 
	 * Expected value format: http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 * 
	 * Note: Effectively it sould be {conserv.url}/{conserv.config.path}
	 */
	public static final String CONFIG_URL = "conserv.config.url";

	/**
	 * (System) property name of the Configuration coordinates in one piece
	 * 
	 * Expected value format: {environment}/{application}/{resource}
	 * 
	 * Note: It takes precedence before CONFIG_ENVIRONMENT, CONFIG_APPLICATION, CONFIG_RESOURCE when set
	 */
	public static final String CONFIG_PATH = "conserv.config.path";

	/**
	 * (System) property name of the Configuration environment coordinate
	 * 
	 * Expected value format: {environment}
	 */
	public static final String CONFIG_ENVIRONMENT = "conserv.config.environment";

	/**
	 * (System) property name of the Configuration application coordinate
	 * 
	 * Expected value format: {application}
	 */
	public static final String CONFIG_APPLICATION = "conserv.config.application";

	/**
	 * (System) property name of the Configuration resource coordinate
	 * 
	 * Expected value format: {resource}
	 */
	public static final String CONFIG_RESOURCE = "conserv.config.resource";

	private final ConservClient client;

	private final String environment;

	private final String application;

	private final String resource;

	/**
	 * Creates own internal ConfigClient instance
	 * 
	 * @param configUrl - http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 */
	public ConservResource(String configUrl) {
		this(parseURL(configUrl));
	}

	/**
	 * Creates own internal ConfigClient instance
	 * 
	 * @param configUrl - http://config.server.com:8080/conserv/{environment}/{application}/{resource}
	 */
	public ConservResource(URL configUrl) {
		//Remove config path from url and initialize ConservClient with it
		String[] elements = parseConfigUrl(configUrl.toString());
		String serverUrl = elements[0];
		String[] coordinates = parseConfigPath(elements[1]);
		environment = coordinates[0];
		application = coordinates[1];
		resource = coordinates[2];

		this.client = new ConservClient(serverUrl);
	}

	/**
	 * Creates own internal ConfigClient instance
	 * 
	 * @param serverUrl - http://config.server.com:8080/conserv
	 * @param configPath - {environment}/{application}/{resource}
	 */
	public ConservResource(String serverUrl, String configPath) {
		this(new ConservClient(serverUrl), configPath);
	}

	/**
	 * Creates own internal ConfigClient instance
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
	 * @param configPath - {environment}/{application}/{resource}
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

	/**
	 * Create instance using custom PropertiesDiscovery
	 */
	public ConservResource(PropertiesDiscovery discovery) {
		this(discovery.discover(true).getProperties());
	}

	/**
	 * @param properties used to build ConservClient and ConservResource
	*/
	public ConservResource(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException("Null properties");
		}
		String configUrl = properties.getProperty(CONFIG_URL);
		String serverUrl = properties.getProperty(ClientSettings.SERVER_URL);
		if (configUrl != null) {

			if (serverUrl != null) {
				if (configUrl.indexOf(serverUrl) == -1) {
					throw new ConservInitException("Clashing values '" + ClientSettings.SERVER_URL + "' '" + serverUrl
							+ "' with '" + CONFIG_URL + "' '" + configUrl + "' found");
				}
			} else {
				String[] elements = parseConfigUrl(configUrl);
				serverUrl = elements[0];
				properties = new Properties(properties); //make properties copy and add SERVER_URL
				properties.setProperty(ClientSettings.SERVER_URL, serverUrl);
			}
		} else { // configUrl == null

			if (serverUrl == null) {
				throw new ConservInitException("Neither '" + ClientSettings.SERVER_URL + "' nor '" + CONFIG_URL
						+ "' property found");
			}
		}
		ClientSettings settings = new ClientSettings(properties);
		this.client = new ConservClient(settings);

		String configPath = properties.getProperty(CONFIG_PATH);
		if (configUrl != null) {
			String[] coordinates = parseConfigPath(parseURL(configUrl).getPath());
			this.environment = coordinates[0];
			this.application = coordinates[1];
			this.resource = coordinates[2];
		} else if (configPath != null) {
			String[] coordinates = parseConfigPath(configPath);
			this.environment = coordinates[0];
			this.application = coordinates[1];
			this.resource = coordinates[2];
		} else {
			this.environment = ClientSettings.getRequired(properties, CONFIG_ENVIRONMENT);
			this.application = ClientSettings.getRequired(properties, CONFIG_APPLICATION);
			this.resource = ClientSettings.getRequired(properties, CONFIG_RESOURCE);
		}
	}

	/**
	 * @param client - provided initialized ConservClient
	 * @param properties used to build ConservResource
	 
	public ConservResource(ConservClient client, Properties properties) {
		if (client == null) {
			throw new IllegalArgumentException("Null client");
		}
		this.client = client;
		String configUrl = properties.getProperty(CONFIG_URL);
		String configPath = properties.getProperty(CONFIG_PATH);
		if (configUrl != null) {
			String[] coordinates = parseConfigPath(parseURL(configUrl).getPath());
			this.environment = coordinates[0];
			this.application = coordinates[1];
			this.resource = coordinates[2];
		} else if (configPath != null) {
			String[] coordinates = parseConfigPath(configPath);
			this.environment = coordinates[0];
			this.application = coordinates[1];
			this.resource = coordinates[2];
		} else {
			this.environment = ClientSettings.getRequired(properties, CONFIG_ENVIRONMENT);
			this.application = ClientSettings.getRequired(properties, CONFIG_APPLICATION);
			this.resource = ClientSettings.getRequired(properties, CONFIG_RESOURCE);
		}

	}
	*/
	/**
	 * @return internally used ConservClient
	 */
	public ConservClient getClient() {
		return client;
	}

	/**
	 * @return Config loaded by ConservClient from Conserver server
	 */
	public Config get() {
		return this.client.getConfig(environment, application, resource);
	}

	/**
	 * Justy because exception can't be properly catched in constructor...
	 */
	public static URL parseURL(String string) {
		try {
			return new URL(string);
		} catch (MalformedURLException mux) {
			throw new ConservInitException("Malformed url: '" + string + "'", mux);
		}
	}

	/**
	 * @return [0] - serverUrl, [1] - configPath
	 */
	public static String[] parseConfigUrl(String strConfigUrl) {
		URL configUrl = parseURL(strConfigUrl);
		String path = configUrl.getPath();
		String[] coordinates = parseConfigPath(path);
		String configPath = coordinates[0] + "/" + coordinates[1] + "/" + coordinates[2];
		String serverUrl = strConfigUrl.substring(0, strConfigUrl.indexOf(configPath) - 1);
		//System.out.println(serverUrl + " " + configPath);
		return new String[] { serverUrl, configPath };
	}

	/**
	 * @return [0] - environment, [1] - application, [2] - resource
	 */
	public static String[] parseConfigPath(String path) {
		int sidx = 0;
		int eidx = path.length();
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
		if (parts.length < 3) { //get last 3 segments
			throw new ConservInitException("Missing configuration elements in path '" + path + "'");
		} else {
			String[] retval = new String[3];
			retval[0] = parts[parts.length - 3];
			if (retval[0].length() == 0) {
				throw new ConservInitException("Environment coordinate is blank in " + path);
			}
			retval[1] = parts[parts.length - 2];
			if (retval[1].length() == 0) {
				throw new ConservInitException("Application coordinate is blank in " + path);
			}
			retval[2] = parts[parts.length - 1];
			if (retval[2].length() == 0) {
				throw new ConservInitException("Resource coordinate is blank in " + path);
			}
			//System.out.println(retval[0] + " " + retval[1] + " " + retval[2]);
			return retval;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + client.getServerUrl() + " " + environment + "/" + application + "/"
				+ resource;
	}

}
