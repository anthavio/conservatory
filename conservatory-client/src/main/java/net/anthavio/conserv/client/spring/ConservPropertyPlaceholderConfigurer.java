package net.anthavio.conserv.client.spring;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.anthavio.conserv.client.ConservClient;
import net.anthavio.conserv.client.ConservException;
import net.anthavio.conserv.client.ConservResource;
import net.anthavio.conserv.model.Config;
import net.anthavio.conserv.model.Property;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


/**
 * Spring 2.0-3.0 
 * 
 * @author martin.vanek
 *
 */
public class ConservPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private final ConservClient client;

	private final ConservResource resource;

	//Note: PropertiesLoaderSupport location field is private and there is not accessor to it - we must keep copy
	private String[] paths;

	public ConservPropertyPlaceholderConfigurer(ConservClient client) {
		this.client = client;
		this.resource = null;
	}

	public ConservPropertyPlaceholderConfigurer(ConservResource resource) {
		this.resource = resource;
		this.client = null;
	}

	@Override
	public void setLocation(Resource location) {
		if (resource != null) {
			throw new IllegalStateException("Do not set Location when initialized with ConservResource");
		}
		super.setLocation(location);
		this.paths = new String[] { convert(location) };
	}

	@Override
	public void setLocations(Resource[] locations) {
		if (resource != null) {
			throw new IllegalStateException("Do not set Locations when initialized with ConservResource");
		}
		super.setLocations(locations);
		if (locations != null) {
			String[] paths = new String[locations.length];
			for (int i = 0; i < locations.length; ++i) {
				paths[i] = convert(locations[i]);
			}
			this.paths = paths;
		}

	}

	protected String convert(Resource location) {
		String path;
		if (client != null) {
			if (location instanceof UrlResource) {
				//URI uri = ((UrlResource)location).getURI();
				throw new IllegalArgumentException("ConservClient is initialized. UrlResource must NOT be used: " + location);
			} else if (location instanceof FileSystemResource) {
				path = ((FileSystemResource) location).getPath();
			} else if (location instanceof DescriptiveResource) {
				path = ((DescriptiveResource) location).getDescription();
			} else {
				throw new IllegalArgumentException("Unsupported Resource " + location.getClass() + " " + location);
			}
		} else {
			if (location instanceof UrlResource == false) {
				throw new IllegalArgumentException("ConservClient is NOT initialized. UrlResource must be used, not "
						+ location.getClass() + " " + location);
			} else {
				try {
					path = ((UrlResource) location).getURL().toString();
				} catch (IOException iox) {
					throw new IllegalArgumentException("Cannot convert " + location, iox);
				}
			}
		}
		if (path.charAt(0) == '/') {
			path = path.substring(1);
		}
		if (path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	@Override
	protected void loadProperties(Properties props) throws IOException {
		if (resource != null) {
			Config config = resource.get();
			convert(config, props);

		} else if (this.paths != null) {
			for (String location : this.paths) {
				if (logger.isInfoEnabled()) {
					String configUrl = client.getServerUrl().toString();
					if (!configUrl.endsWith("/")) {
						configUrl = configUrl + "/" + location;
					}
					logger.info("Loading properties from " + configUrl);
				}
				try {
					String[] split = location.split("/");
					String envCode = split[0];
					String appCode = split[1];
					String resCode = split[2];
					Config config = client.getConfig(envCode, appCode, resCode);
					convert(config, props);
				} catch (ConservException cx) {
					throw cx;
					/*
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
						}
					} else {
						throw ex;
					}
					*/

				}
			}
		}
	}

	protected void convert(Config config, Properties props) {
		List<Property> properties = config.getProperties();
		for (Property property : properties) {
			//TODO unescape as in DefaultPropertiesPersister#doLoad()
			props.put(property.getName(), property.getValue());
		}
	}
}
