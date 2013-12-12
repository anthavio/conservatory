package com.anthavio.conserv.client.spring;

import org.springframework.core.env.EnumerablePropertySource;

import com.anthavio.conserv.client.ConservClient;
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
public class ConservPropertiesSource extends EnumerablePropertySource<ConservClient> {

	private ConservClient client;

	private String environment;
	private String application;
	private String resource;

	public ConservPropertiesSource(ConservClient client, String environment, String application, String resource) {
		this("conserve", client, environment, application, resource);
	}

	public ConservPropertiesSource(String name, ConservClient client, String environment, String application,
			String resource) {
		super(name, client);
		this.client = client;
		this.environment = environment;
		this.application = application;
		this.resource = resource;
	}

	@Override
	public Object getProperty(String name) {
		Property property = client.getConfig(environment, application, resource).getProperty(name);
		if (property != null) {
			return property.getValue();
		} else {
			return null;
		}
	}

	@Override
	public String[] getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
