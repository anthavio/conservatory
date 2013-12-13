package com.anthavio.conserv.client.spring;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.anthavio.conserv.client.ConservClient;
import com.anthavio.conserv.client.ConservException;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * Spring 2.0-3.0 
 * 
 * @author martin.vanek
 *
 */
public class ConservPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private ConservClient client;

	//Note: PropertiesLoaderSupport location field is private and there is not accessor to it - we must keep copy
	private String[] paths;

	public ConservPropertyPlaceholderConfigurer(ConservClient client) {
		this.client = client;
	}

	@Override
	public void setLocation(Resource location) {
		super.setLocation(location);
		this.paths = new String[] { convert(location) };
	}

	@Override
	public void setLocations(Resource[] locations) {
		super.setLocations(locations);
		if (locations != null) {
			String[] paths = new String[locations.length];
			for (int i = 0; i < locations.length; ++i) {
				paths[i] = convert(locations[i]);
			}
			this.paths = paths;
		}

	}

	private String convert(Resource location) {
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
		if (this.paths != null) {
			for (String location : this.paths) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties from " + client.getServerUrl() + location);
				}
				try {
					String[] split = location.split("/");
					String envCode = split[0];
					String appCode = split[1];
					String resCode = split[2];
					Config config = client.getConfig(envCode, appCode, resCode);
					List<Property> properties = config.getProperties();
					for (Property property : properties) {
						//TODO unescape as in DefaultPropertiesPersister#doLoad()
						props.put(property.getName(), property.getValue());
					}

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
}
