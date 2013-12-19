package net.anthavio.conserv.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import net.anthavio.conserv.client.ConservClient;
import net.anthavio.conserv.client.ConservResource;
import net.anthavio.discovery.PropertiesDiscovery;

/**
 * In weapps, it should be used like here - https://gist.github.com/rponte/3989915
 * 
 * @author martin.vanek
 *
 */
public class ConservContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PropertiesDiscovery discovery;

	/**
	 * Use default PropertiesDiscovery
	 */
	public ConservContextInitializer() {
		this.discovery = null;
	}

	/**
	 * Allow custom PropertiesDiscovery
	 */
	public ConservContextInitializer(PropertiesDiscovery discovery) {
		this.discovery = discovery;
	}

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing " + context);
		}

		ConservResource resource;
		try {
			resource = context.getBean(ConservResource.class);
		} catch (NoSuchBeanDefinitionException nsbx) {
			resource = null;
		}

		if (resource == null) {
			if (this.discovery != null) {
				resource = new ConservResource(discovery);
			} else {
				resource = ConservResource.Default();
			}
			//register only if they not already exist in Spring context
			if (logger.isDebugEnabled()) {
				logger.debug("Registering singletons 'ConservResource' and 'ConservClient' into Spring context");
			}
			context.getBeanFactory().registerSingleton(ConservResource.class.getSimpleName(), resource);
			context.getBeanFactory().registerSingleton(ConservClient.class.getSimpleName(), resource.getClient());
		}

		ConservPropertiesSource source = new ConservPropertiesSource(resource);
		context.getEnvironment().getPropertySources().addFirst(source);

		if (logger.isDebugEnabled()) {
			logger.debug("Initialized " + context + " from " + source.getSource());
		}
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
