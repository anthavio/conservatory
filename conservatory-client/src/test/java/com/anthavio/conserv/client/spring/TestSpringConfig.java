package com.anthavio.conserv.client.spring;

import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;

/**
 * 
 * @author martin.vanek
 *
 */
@Configuration
public class TestSpringConfig {

	@Value("${string.property}")
	private String stringProperty;

	@Value("${url.property}")
	private URL urlProperty;

	public String getStringProperty() {
		return stringProperty;
	}

	public URL getUrlProperty() {
		return urlProperty;
	}

	@Bean
	public static PropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
		int httpPort = Integer.parseInt(System.getProperty("http.port"));
		configurer.setLocation(new UrlResource("http://localhost:" + httpPort + "/"));
		return configurer;
		//ConfigurableListableBeanFactory beanFactory= null;
		//configurer.postProcessBeanFactory(beanFactory);
	}
	/*
	@Bean
	public static PropertySourcesPlaceholderConfigurer sourcePlaceholderConfigurer() throws IOException {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		int httpPort = Integer.parseInt(System.getProperty("http.port"));
		configurer.setLocation(new UrlResource("http://localhost:" + httpPort + "/"));
		return configurer;
		//ConfigurableListableBeanFactory beanFactory= null;
		//configurer.postProcessBeanFactory(beanFactory);
	}
	*/
}
