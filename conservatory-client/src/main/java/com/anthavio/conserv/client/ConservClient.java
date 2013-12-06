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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthavio.conserv.client.ConfigParser.Format;
import com.anthavio.conserv.client.ConservLoader.LoadResult;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * 
 * GET http://somewhere.com:8080/conserve/config/{environment}/{application}/{resource}
 * 
 * @author martin.vanek
 *
 */
public class ConservClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ClientSettings settings;

	private List<PropertyChangeListener> listeners;

	private final Map<String, Config> configCache = new HashMap<String, Config>();

	public ConservClient(String serverUrl) {
		this(new ClientSettings(serverUrl));
	}

	public ConservClient(ClientSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("Null settings");
		}
		this.settings = settings;
		if (settings.getConfigParser() == null) {
			settings.setConfigParser(new JaxbConfigParser());
		}
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
			throw new IllegalStateException("Malformed resource url: " + configPath, mux);
		}

		String fileName = configPath.replace('/', '-') + "." + settings.getConfigParser().getFormat();
		try {

			return load(resourceUrl, fileName, false);

		} catch (Exception x) {
			if (settings.getConfigMemoryCaching()) {
				Config config = configCache.get(fileName);
				if (config != null) {
					logger.error("Configuration server load failed", x);
					logger.info("Configuration loaded from memory " + fileName);
					return config;
				}
			}
			File fileConfig = new File(settings.getConfigDirectory(), fileName);
			if (settings.getConfigFileCaching() && fileConfig.exists()) {
				try {
					Config config = load(fileConfig);
					logger.error("Configuration server load failed", x);
					logger.info("Configuration loaded from file " + fileConfig);
					return config;
				} catch (Exception fx) {
					logger.error("Configuration local load failed", fx);
					throw new IllegalStateException("Configuration server and file load failed", x);
				}
			}
			//no local copy found
			throw new IllegalStateException("Configuration server load failed", x);
		}
	}

	/**
	 * Load from remote server, also notify listeners 
	 */
	private Config load(URL resourceUrl, String fileName, boolean notify) throws IOException {
		File fileConfig = new File(settings.getConfigDirectory(), fileName);
		LoadResult result = settings.getConservLoader().load(resourceUrl, settings);
		Config config = parse(result.getContent(), result.getMimeType());
		Config oldConfig = null;
		if (settings.getConfigMemoryCaching()) {
			oldConfig = configCache.get(fileName);
			configCache.put(fileName, config);
		}
		if (settings.getConfigFileCaching()) {
			//load old only if not taken from memory
			if (oldConfig == null && fileConfig.exists()) {
				try {
					oldConfig = load(fileConfig);
				} catch (Exception x) {
					logger.warn("Failed to load previous config from " + fileName, x);
				}
			}
			//save always new version (even when no changes exist)
			save(fileConfig, result.getContent());
		}
		if (notify && this.listeners != null && oldConfig != null
				&& !oldConfig.getCreatedAt().equals(config.getCreatedAt())) {
			checkChangesAndNotifyListeners(oldConfig, config);
		}
		return config;
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
		ConfigParser parser = settings.getConfigParser();
		Format format = parser.getFormat();
		if (!mimeType.equals(format.getMimeType())) {
			throw new IllegalStateException("Wrong mime type " + mimeType + " for " + parser.getFormat());
		} else {
			char fchar = getFirstCharacter(content);
			if (!format.supports(fchar)) {
				throw new IllegalStateException("Content starts with invalid character " + fchar + " for " + parser.getFormat());
			} else {
				return parser.parse(new StringReader(content));
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
