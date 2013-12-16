package com.anthavio.conserv.client.spring;

import java.util.List;

import org.springframework.core.env.EnumerablePropertySource;

import com.anthavio.conserv.client.ConservResource;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

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

	private ConservResource resource;

	private transient Config config; //cache

	public ConservPropertiesSource(ConservResource resource) {
		this("conserve", resource);
	}

	public ConservPropertiesSource(String name, ConservResource resource) {
		super(name, resource);
		this.resource = resource;
	}

	private Config getConfig() {
		if (config == null) {
			config = resource.get();
		}
		return config;
	}

	@Override
	public Object getProperty(String name) {
		Property property = getConfig().getProperty(name);
		if (property != null) {
			return property.getValue();
		} else {
			return null;
		}
	}

	@Override
	public String[] getPropertyNames() {
		List<Property> properties = getConfig().getProperties();
		String[] names = new String[properties.size()];
		for (int i = 0; i < properties.size(); ++i) {
			names[i] = properties.get(i).getName();
		}
		return names;
	}
}
