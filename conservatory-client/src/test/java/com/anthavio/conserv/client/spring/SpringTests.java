package com.anthavio.conserv.client.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.fest.assertions.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.anthavio.conserv.client.ClientSettings;
import com.anthavio.conserv.client.ConservClient;
import com.anthavio.conserv.client.ConservResource;
import com.anthavio.conserv.client.FakeServer;
import com.anthavio.conserv.client.TestData;

/**
 * 
 * @author martin.vanek
 *
 */
public class SpringTests {

	private static final String SP_HTTP_PORT = "http.port";

	//private static final String SP_CONSERV_URL = "conserv.url";

	//private static final String SP_CONFIG_URL = "conserv.config.url";

	//test/example/properties

	private FakeServer server = new FakeServer(false);

	@BeforeClass
	public void beforeClass() {
		server.start();
		System.setProperty(SP_HTTP_PORT, server.getHttpPort() + "");
	}

	@Test
	public void testSpring31Custom() throws Exception {

		//server.setFormat(Format.XML);

		//Properties properties =  System.getProperties();
		Properties properties = new Properties();
		properties.setProperty(ClientSettings.SERVER_URL, "http://localhost:" + server.getHttpPort() + "/conserv");
		//properties.setProperty(ConservResource.CONFIG_PATH, "/test/example/properties/");
		properties.setProperty(ConservResource.CONFIG_ENVIRONMENT, "test");
		properties.setProperty(ConservResource.CONFIG_APPLICATION, "example");
		properties.setProperty(ConservResource.CONFIG_RESOURCE, "properties");

		File file = save(properties);
		System.setProperty("conserv.file", file.toString());

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Spring31Custom.class);
		ConservContextInitializer initializer = new ConservContextInitializer();
		initializer.initialize(ctx);
		ctx.refresh();
		Spring31Custom config = ctx.getBean(Spring31Custom.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

		Assertions.assertThat(config.getEnvProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

		Assertions.assertThat(config.getEnvProperty("string.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

	}

	private File save(Properties properties) throws IOException {
		File file = File.createTempFile("conserv", "properties");
		file.deleteOnExit();
		FileOutputStream stream = new FileOutputStream(file);
		properties.store(stream, "test tetst ttetete");
		stream.close();
		return file;
	}

	//@Test
	public void testSpring31Basic() throws Exception {

		//server.setFormat(Format.PLAIN);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring31Basic.class);
		Spring31Basic config = ctx.getBean(Spring31Basic.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

		//server.setFormat(Format.XML);

		Assertions.assertThat(config.getEnvProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

	}

	//@Test
	public void testSpring30Custom() throws Exception {

		//server.setFormat(Format.XML);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Custom.class);
		Spring30Custom config = ctx.getBean(Spring30Custom.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

	}

	//@Test
	public void testSpring30Basic() throws Exception {

		//server.setFormat(Format.PLAIN);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Basic.class);
		Spring30Basic config = ctx.getBean(Spring30Basic.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());
	}

	public static class Abstract31Config extends AbstractConfig {

		@Autowired
		private Environment environment;

		public String getEnvProperty(String name) {
			return environment.getProperty(name);
		}

		/**
		 * Value will be provided via Environment and @PropertySource
		 */
		@Bean
		public URLConnection URLConnectionEnv() throws IOException {
			return new URL(environment.getProperty("url.property")).openConnection();
		}
	}

	@Configuration
	public static class AbstractConfig {

		@Value("${string.property}")
		private String stringProperty;

		@Value("${url.property}")
		private URL urlProperty;

		public String getStringPropertyValue() {
			return stringProperty;
		}

		public URL getUrlPropertyValue() {
			return urlProperty;
		}

		/**
		 * @Value will be provided via PropertyPlaceholder...Configurer
		 */
		@Bean
		public URLConnection getURLConnection(@Value("${url.property}") String urlProperty) throws IOException {
			return new URL(urlProperty).openConnection();
		}

		@Bean
		public static ConservResource ConservResource() {
			return ConservResource.Default();
		}

	}

	//We will not use @PropertySource, but ConservContextInitializer 
	public static class Spring31Custom extends Abstract31Config {

		/**
		 *  Custom Spring 2.0+ ConservPropertyPlaceholderConfigurer - fills @Value(${"some.property"})
		 */
		@Bean
		public static ConservPropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			ConservClient client = ConservClient.Default();
			ConservPropertyPlaceholderConfigurer configurer = new ConservPropertyPlaceholderConfigurer(client);
			configurer.setLocation(new FileSystemResource("/test/example/properties"));
			return configurer;
		}

	}

	public static class Spring30Custom extends AbstractConfig {

		/**
		 * Custom Spring 2.0+ ConservPropertyPlaceholderConfigurer - fills @Value(${"some.property"})
		 * Using ConservClient internally - Caching and authentication can be used
		 */
		@Bean
		public static ConservPropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {

			ConservResource resource = ConservResource.Default();
			ConservPropertyPlaceholderConfigurer configurer = new ConservPropertyPlaceholderConfigurer(resource);
			configurer.setLocation(new FileSystemResource("/test/example/properties"));
			return configurer;
		}

	}

	//fills Environment - System Property - no authentication and caching
	@PropertySource("${" + ConservResource.CONFIG_URL + "}")
	public static class Spring31Basic extends Abstract31Config {

		/**
		 * Standard Spring 3.1+ PropertySourcesPlaceholderConfigurer - fills @Value(${"some.property"})
		 */
		@Bean
		public static PropertySourcesPlaceholderConfigurer sourcePlaceholderConfigurer() throws IOException {
			PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
			String serverUrl = System.getProperty(ClientSettings.SERVER_URL);
			configurer.setLocation(new UrlResource(serverUrl));
			return configurer;
		}
	}

	public static class Spring30Basic extends AbstractConfig {

		/**
		 * Standard Spring 2.0+ PropertyPlaceholderConfigurer - fills @Value(${"some.property"})
		 * Loads properties in text/plain format direcly using raw java HttpURLConnection  
		 * Caching and authentication cannot happen since ConservClient is NOT involved  
		 */
		@Bean
		public static PropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
			String serverUrl = System.getProperty(ClientSettings.SERVER_URL);
			configurer.setLocation(new UrlResource(serverUrl));
			return configurer;
		}

	}

}
