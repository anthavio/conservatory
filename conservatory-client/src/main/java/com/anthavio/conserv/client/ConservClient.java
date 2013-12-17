package com.anthavio.conserv.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthavio.conserv.client.ConfigParser.Format;
import com.anthavio.conserv.client.ConservLoader.LoadResult;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;
import com.anthavio.discovery.Discovery;
import com.anthavio.discovery.Discovery.Result;

/**
 * 
 * GET http://somewhere.com:8080/conserve/config/{environment}/{application}/{resource}
 * 
 * @author martin.vanek
 *
 */
public class ConservClient {

	/**
	 * @return ConservResource discovered with default location techniques and parameters
	 * @throws ConservInitException when default configuration cannot be located  
	 */
	public static ConservClient Default() throws ConservInitException {
		Result result = Discover();
		ClientSettings settings = new ClientSettings(result.getProperties());
		ConservClient client = new ConservClient(settings);
		return client;
	}

	public static Result Discover() {
		Result finding = Discovery.Builder().system("conserv.url").filepath("conserv.file").classpath("conserv.properties")
				.discover();
		if (finding == null) {
			throw new ConservInitException("Conserv client configuration properties not found");
		}
		return finding;
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ClientSettings settings;

	private List<PropertyChangeListener> listeners;

	private final Map<String, Config> configCache = new HashMap<String, Config>();

	public ConservClient(ClientSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("Null settings");
		}
		this.settings = settings;
		if (settings.getConfigParser() == null) {
			settings.setConfigParser(new JaxbConfigParser());
		}
	}

	public ConservClient(String serverUrl) {
		this(new ClientSettings(serverUrl));
	}

	public URL getServerUrl() {
		return settings.getServerUrl();
	}

	public void addListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<PropertyChangeListener>();
		}
		listeners.add(listener);
	}

	public Config getConfig(String environment, String application, String resource) {
		if (environment == null || environment.length() == 0) {
			throw new IllegalArgumentException("Environment is blank: " + environment);
		}
		if (application == null || application.length() == 0) {
			throw new IllegalArgumentException("Application is blank: " + application);
		}
		if (resource == null || resource.length() == 0) {
			throw new IllegalArgumentException("Resource is blank: " + resource);
		}
		String configPath = environment + "/" + application + "/" + resource;

		String fullUrl = settings.getServerUrl().toString();
		if (!fullUrl.endsWith("/")) {
			fullUrl += "/";
		}
		fullUrl += configPath;
		URL resourceUrl;
		try {
			//may happen when environment/application/resource contains something illegal
			resourceUrl = new URL(fullUrl);
		} catch (MalformedURLException mux) {
			throw new ConservLoadException("Malformed resource url: " + configPath, mux);
		}

		String configName = configPath.replace('/', '-') + "." + settings.getConfigParser().getFormat();

		Config configOld = null;
		if (settings.getConfigMemoryCaching()) {
			configOld = configCache.get(configName);
		}
		File fileConfig = new File(settings.getConfigDirectory(), configName);
		if (configOld == null) {
			if (settings.getConfigFileCaching() && fileConfig.exists()) {
				try {
					configOld = load(fileConfig);
				} catch (Exception x) {
					logger.warn("Failed to load previous config from " + configName, x);
				}
			}
		}

		try {

			//Config configNew = load(resourceUrl, configName, configOld);

			Date lastModified = configOld != null ? configOld.getCreatedAt() : null;
			LoadResult result = settings.getConservLoader().load(resourceUrl, settings, lastModified);
			if (result.getHttpCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
				return configOld; //Leave now
			}

			Config configNew = parse(result.getContent(), result.getMimeType());
			if (settings.getConfigMemoryCaching()) {
				configCache.put(configName, configNew);
			}
			if (settings.getConfigFileCaching()) {
				//save always new version (even when no changes exist)
				save(fileConfig, result.getContent());
			}
			/*
			if (bNodify && this.listeners != null && configOld != null
					&& !configOld.getCreatedAt().equals(configNew.getCreatedAt())) {
				checkChangesAndNotifyListeners(configOld, configNew);
			}
			*/
			return configNew;

		} catch (Exception x) {
			if (configOld != null) {
				logger.error("Configuration server load failed. Returning cached configuration", x);
				return configOld;
			} else {
				throw new ConservLoadException("Configuration server load failed", x);
			}
		}
	}

	/**
	 * If no new configuration returned from server, it returns old 
	 */
	private Config load(URL resourceUrl, String configName, Config configOld) throws IOException {
		File fileConfig = new File(settings.getConfigDirectory(), configName);
		Date lastModified = configOld != null ? configOld.getCreatedAt() : null;
		LoadResult result = settings.getConservLoader().load(resourceUrl, settings, lastModified);
		if (result.getHttpCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
			return configOld; //Leave now
		}

		Config configNew = parse(result.getContent(), result.getMimeType());
		if (settings.getConfigMemoryCaching()) {
			configCache.put(configName, configNew);
		}
		if (settings.getConfigFileCaching()) {
			//save always new version (even when no changes exist)
			save(fileConfig, result.getContent());
		}
		return configNew;
	}

	private Config load(File file) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
			String extension = file.getName().substring(file.getName().length() - 3);
			Format format = Format.valueOf(extension);
			return parse(sb.toString(), format.getMimeType());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Never ever throw any exception from this method 
	 */
	private void save(File file, String content) {
		try {
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
				writer.write(content); //TODO write content in more chunks...
				writer.close();
				logger.debug("Configuration saved into " + file);
			} catch (IOException iox) {
				logger.warn("Error writing " + file, iox);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException iox) {
						logger.warn("Error closing writer for " + file, iox);
					}
				}
			}
		} catch (Exception x) {
			logger.warn("Error saving " + file, x);
		}
	}

	private Config parse(String content, String mimeType) throws IOException {
		//XXX kind of hackish...
		//if (Format.PLAIN.getMimeType().equals(mimeType)) {
		//	return new JavaPropsParser().parse(new StringReader(content));
		//}

		ConfigParser parser = settings.getConfigParser();
		Format format = parser.getFormat();
		if (!mimeType.equals(format.getMimeType())) {
			throw new ConservLoadException("Wrong mime type " + mimeType + " for parser " + parser.getFormat());
		} else {
			char fchar = getFirstCharacter(content);
			if (!format.supports(fchar)) {
				throw new ConservLoadException("Content starts with invalid character " + fchar + " for " + parser.getFormat());
			} else {
				return parser.parse(new StringReader(content));
			}
		}

	}

	private void checkChangesAndNotifyListeners(Config oldConfig, Config newConfig) {
		//check for changed and removed values
		for (Property oldProperty : oldConfig.getProperties()) {
			Property newProperty = newConfig.getProperty(oldProperty.getName());
			if (newProperty == null) {
				for (PropertyChangeListener listener : listeners) {
					try {
						listener.propertyRemoved(oldProperty);
					} catch (Exception x) {
						logger.error("Property change listener notification failed", x);
					}
				}
			} else if (!newProperty.getValue().equals(oldProperty.getValue())) {
				for (PropertyChangeListener listener : listeners) {
					try {
						listener.propertyChanged(oldProperty, newProperty);
					} catch (Exception x) {
						logger.error("Property change listener notification failed", x);
					}
				}
			}
		}
		//check for added values only (changed values are already checked)
		for (Property newProperty : newConfig.getProperties()) {
			Property oldProperty = newConfig.getProperty(newProperty.getName());
			if (oldProperty == null) {
				for (PropertyChangeListener listener : listeners) {
					try {
						listener.propertyAdded(newProperty);
					} catch (Exception x) {
						logger.error("Property change listener notification failed", x);
					}
				}
			}
		}

	}

	/**
	 * Get first non whitespace character from content
	 */
	private char getFirstCharacter(String content) {
		for (int i = 0; i < content.length(); ++i) {
			char c = content.charAt(i);
			if (!Character.isWhitespace(c)) {
				return c;
			}
		}
		return ' ';
	}

}
