package net.anthavio.conserv.client.spring;

import java.util.List;

import net.anthavio.conserv.client.ConservResource;
import net.anthavio.conserv.model.Config;
import net.anthavio.conserv.model.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;


/**
 * 
 * @author martin.vanek
 * 
 * Spring 3.1+ PropertySource
 * 
 * Inspired by - http://stackoverflow.com/a/13470704/429946
 * 
 * http://stackoverflow.com/questions/2561947/spring-to-understand-properties-in-yaml
 *
 */
public class ConservPropertiesSource extends EnumerablePropertySource<ConservResource> {

	private static final Logger logger = LoggerFactory.getLogger(ConservPropertiesSource.class);

	private ConservResource resource;

	private transient Config config; //cache

	public ConservPropertiesSource(ConservResource resource) {
		this("conserve", resource);
	}

	public ConservPropertiesSource(String name, ConservResource resource) {
		super(name, resource);
		this.resource = resource;
	}

	private Config getConfig(String name) {
		if (config == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Lazy loading config on '" + name + "' property request");
			}
			config = resource.get();
		}
		return config;
	}

	@Override
	public Object getProperty(String name) {
		Property property = getConfig(name).getProperty(name);
		if (property != null) {
			String value = property.getValue();
			if (logger.isTraceEnabled()) {
				logger.trace("Property '" + name + "' value '" + value + "'");
			}
			return value;
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("Property '" + name + "' not found");
			}
			return null;
		}

	}

	@Override
	public String[] getPropertyNames() {
		List<Property> properties = getConfig("ConservPropertiesSource.getPropertyNames()").getProperties();
		String[] names = new String[properties.size()];
		for (int i = 0; i < properties.size(); ++i) {
			names[i] = properties.get(i).getName();
		}
		return names;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + resource;
	}
}
