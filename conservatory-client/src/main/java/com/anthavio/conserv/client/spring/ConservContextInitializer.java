package com.anthavio.conserv.client.spring;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.anthavio.conserv.client.ConservResource;

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

		ConservResource resource;
		try {
			resource = context.getBean(ConservResource.class);
		} catch (NoSuchBeanDefinitionException nsbx) {
			resource = null;
		}
		if (resource == null) {
			resource = ConservResource.Default();
		}

		ConservPropertiesSource source = new ConservPropertiesSource(resource);
		context.getEnvironment().getPropertySources().addFirst(source);
	}

	/**
	 * Get ConservClient from Spring context
	 * 
	 * @return ConservClient instance or null
	 
	private ConservResource getFromSpringContext(ConfigurableApplicationContext context) {
		ConservResource resource = context.getBean(ConservResource.class);
		if (resource == null) {
			ClientSettings settings = context.getBean(ClientSettings.class);
			if (settings != null) {
				return new ConservClient(settings);
			}
		}
		return resource;
	}
	*/
}
