package net.anthavio.conserv.client.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import net.anthavio.conserv.client.ClientSettings;
import net.anthavio.conserv.client.ConservClient;
import net.anthavio.conserv.client.ConservResource;
import net.anthavio.conserv.client.FakeServer;
import net.anthavio.conserv.client.TestData;
import net.anthavio.conserv.client.TestingPropertiesFinder;
import net.anthavio.conserv.client.spring.ConservContextInitializer;
import net.anthavio.conserv.client.spring.ConservPropertyPlaceholderConfigurer;

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


/**
 * 
 * @author martin.vanek
 *
 */
public class SpringTests {

	private static final String SP_HTTP_PORT = "http.port";

	private FakeServer server = new FakeServer(false);

	@BeforeClass
	public void beforeClass() {
		server.start();
		System.getProperties().put(SP_HTTP_PORT, server.getHttpPort());
	}

	@Test
	public void spring31Custom() throws Exception {

		//Properties properties =  System.getProperties();
		Properties properties = new Properties();
		//properties.setProperty(ClientSettings.SERVER_URL, "http://localhost:" + server.getHttpPort() + "/conserv");
		//properties.setProperty(ConservResource.CONFIG_PATH, "/test/example/properties/");
		//properties.setProperty(ConservResource.CONFIG_ENVIRONMENT, "test");
		//properties.setProperty(ConservResource.CONFIG_APPLICATION, "example");
		//properties.setProperty(ConservResource.CONFIG_RESOURCE, "properties");

		String configUrl = "http://localhost:" + server.getHttpPort() + "/conserv/test/example/properties";
		properties.setProperty(ConservResource.CONFIG_URL, configUrl);

		File file = save(properties);
		//System.setProperty("conserv.file", file.toString());
		System.setProperty(TestingPropertiesFinder.SYSPROP_NAME, file.toString());

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

		Assertions.assertThat(config.getSpringEnvProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

		Assertions.assertThat(config.getSpringEnvProperty("string.property")).isEqualTo(
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
	public void spring31Basic() throws Exception {

		String configUrl = "http://localhost:" + server.getHttpPort() + "/conserv/test/example/properties";
		System.setProperty(ConservResource.CONFIG_URL, configUrl);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring31Basic.class);
		Spring31Basic config = ctx.getBean(Spring31Basic.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

		Assertions.assertThat(config.getSpringEnvProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

	}

	//@Test
	public void spring30Custom() throws Exception {

		String serverUrl = "http://localhost:" + server.getHttpPort() + "/conserv";
		String configPath = "/test/example/properties";
		String configUrl = serverUrl + configPath;

		System.clearProperty(ConservResource.CONFIG_URL);

		Properties properties = System.getProperties();
		//Properties properties = new Properties();
		properties.setProperty(ClientSettings.SERVER_URL, serverUrl);
		properties.setProperty(ConservResource.CONFIG_PATH, configUrl);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Custom.class);
		Spring30Custom config = ctx.getBean(Spring30Custom.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

	}

	//@Test
	public void spring30Basic() throws Exception {

		String configUrl = "http://localhost:" + server.getHttpPort() + "/conserv/test/example/properties";
		System.setProperty(ConservResource.CONFIG_URL, configUrl);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Basic.class);
		Spring30Basic config = ctx.getBean(Spring30Basic.class);

		Assertions.assertThat(config.getUrlPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringPropertyValue()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());
	}

	/**
	 * Since Spring 3.1 Environment abstraction is avaliable
	 */
	public static class Abstract31Config extends AbstractConfig {

		@Autowired
		private Environment environment;

		public String getSpringEnvProperty(String name) {
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
		 
		@Bean
		public URLConnection getURLConnection(@Value("${url.property}") String urlProperty) throws IOException {
			return new URL(urlProperty).openConnection();
		}
		*/
		/*
		@Bean
		public static ConservResource ConservResource() {
			return ConservResource.Default();
		}
		*/

	}

	//ConservContextInitializer used instead of @PropertySource  
	public static class Spring31Custom extends Abstract31Config {

		/**
		 *  Custom Spring 2.0+ ConservPropertyPlaceholderConfigurer - fills @Value(${"some.property"})
		 *  
		 *  Using ConservClient internally - Caching and authentication can be used
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
		 * 
		 * Using ConservClient internally - Caching and authentication can be used
		 */
		@Bean
		public static ConservPropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			ConservResource resource = ConservResource.Default();
			ConservPropertyPlaceholderConfigurer configurer = new ConservPropertyPlaceholderConfigurer(resource);
			//configurer.setLocation(new FileSystemResource("/test/example/properties"));
			return configurer;
		}

	}

	/**
	 * @PropertySource Populates Spring Environment properties 
	 * - It is staticaly configured to use url from System Property
	 * - Loads properties in text/plain format direcly using raw java HttpURLConnection  
	 * - Caching and authentication cannot happen since ConservClient is NOT involved
	 */
	@PropertySource("${" + ConservResource.CONFIG_URL + "}")
	public static class Spring31Basic extends Abstract31Config {

		/**
		 * Standard Spring 3.1+ PropertySourcesPlaceholderConfigurer 
		 * 
		 * - Injects @Value(${"some.property"}) and other placeholders
		 * - Loads properties in text/plain format direcly using raw java HttpURLConnection  
		 * - Caching and authentication cannot happen since ConservClient is NOT involved
		 */
		@Bean
		public static PropertySourcesPlaceholderConfigurer sourcePlaceholderConfigurer() throws IOException {
			PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
			String configUrl = System.getProperty(ConservResource.CONFIG_URL);
			configurer.setLocation(new UrlResource(configUrl));
			return configurer;
		}
	}

	public static class Spring30Basic extends AbstractConfig {

		/**
		 * Standard Spring 2.0+ PropertyPlaceholderConfigurer 
		 * 
		 * - Injects @Value(${"some.property"}) and other placeholders
		 * - Loads properties in text/plain format direcly using raw java HttpURLConnection  
		 * - Caching and authentication cannot happen since ConservClient is NOT involved
		 */
		@Bean
		public static PropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
			String configUrl = System.getProperty(ConservResource.CONFIG_URL);
			configurer.setLocation(new UrlResource(configUrl));
			return configurer;
		}

	}

}
