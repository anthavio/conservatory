package com.anthavio.conserv.client.spring;

import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.anthavio.conserv.client.ClientSettings;
import com.anthavio.conserv.client.ConservClient;
import com.anthavio.conserv.client.ConservInitException;
import com.anthavio.discovery.DiscoveryBuilder;

/**
 * In weapps, it should be used like here - https://gist.github.com/rponte/3989915
 * 
 * @author martin.vanek
 *
 */
public class ConservContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	// private static Logger LOG = LoggerFactory.getLogger(ConfigurableApplicationContext.class);

	@Override
	public void initialize(ConfigurableApplicationContext context) {

		//ConservClient client = getFromSpringContext(context);

		Properties properties = new DiscoveryBuilder().system("conserv.url").filepath("conserv.file")
				.classpath("/conserv.properties").discover();
		if (properties == null) {
			throw new ConservInitException("Conserv client configuration not discovered");
		}
		ClientSettings settings = new ClientSettings(properties);
		ConservClient client = new ConservClient(settings);
		String path = settings.getServerUrl().getPath();
		String[] parts = path.split("/");

		String environment = properties.getProperty("conserv.environment");
		String application;
		String resource;
		if (environment != null) {
			environment = getRequired(properties, "conserv.environment");
			application = getRequired(properties, "conserv.application");
			resource = getRequired(properties, "conserv.resource");
		} else {
			//try to get from URL 
			if (parts.length >= 3) {
				environment = parts[parts.length - 3];
				application = parts[parts.length - 2];
				resource = parts[parts.length - 1];
			} else {
				throw new ConservInitException("Missing configuration specification in properties or url");
			}
		}

		ConservPropertiesSource source = new ConservPropertiesSource(client, environment, application, resource);
		context.getEnvironment().getPropertySources().addFirst(source);
	}

	private String getRequired(Properties properties, String name) {
		String value = properties.getProperty(name);
		if (value == null || value.length() == 0) {
			throw new ConservInitException("Required Conserv property " + name + " not found");
		} else {
			return value;
		}
	}

	/**
	 * Get ConservClient from Spring context
	 * 
	 * @return ConservClient instance or null
	 */
	private ConservClient getFromSpringContext(ConfigurableApplicationContext context) {
		ConservClient client = context.getBean(ConservClient.class);
		if (client == null) {
			ClientSettings settings = context.getBean(ClientSettings.class);
			if (settings != null) {
				return new ConservClient(settings);
			}
		}
		return client;
	}

}
