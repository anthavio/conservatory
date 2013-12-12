package com.anthavio.conserv.client.spring;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.fest.assertions.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.anthavio.conserv.client.ClientSettings;
import com.anthavio.conserv.client.ConservClient;
import com.anthavio.conserv.client.FakeServer;
import com.anthavio.conserv.client.TestData;

/**
 * 
 * @author martin.vanek
 *
 */
public class SpringTests {

	private FakeServer server = new FakeServer(false);

	@BeforeClass
	public void beforeClass() {
		server.start();
		System.setProperty("http.port", server.getHttpPort() + "");
	}

	@Test
	public void testSpring31Custom() throws Exception {

		//server.setFormat(Format.XML);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring31Custom.class);
		Spring31Custom config = ctx.getBean(Spring31Custom.class);

		Assertions.assertThat(config.getUrlProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

		Assertions.assertThat(config.getEnvProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

		Assertions.assertThat(config.getEnvProperty("string.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

	}

	@Test
	public void testSpring31Basic() throws Exception {

		//server.setFormat(Format.PLAIN);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring31Basic.class);
		Spring31Basic config = ctx.getBean(Spring31Basic.class);

		Assertions.assertThat(config.getUrlProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());

		//server.setFormat(Format.XML);

		Assertions.assertThat(config.getEvnProperty("url.property")).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asString());

	}

	@Test
	public void testSpring30Custom() throws Exception {

		//server.setFormat(Format.XML);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Custom.class);
		Spring30Custom config = ctx.getBean(Spring30Custom.class);

		Assertions.assertThat(config.getUrlProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());
	}

	@Test
	public void testSpring30Basic() throws Exception {

		//server.setFormat(Format.PLAIN);

		ApplicationContext ctx = new AnnotationConfigApplicationContext(Spring30Basic.class);
		Spring30Basic config = ctx.getBean(Spring30Basic.class);

		Assertions.assertThat(config.getUrlProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("url.property").asUrl());

		Assertions.assertThat(config.getStringProperty()).isEqualTo(
				TestData.getDefaultConfig().getProperty("string.property").asString());
	}

	@Configuration
	public static class AbstractConfig {

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

		protected static String getConservUrl() {
			int httpPort = Integer.parseInt(System.getProperty("http.port"));
			return "http://localhost:" + httpPort + "/conserv";
		}

		@Bean
		public static ConservClient ConservClient() {
			ClientSettings settings = new ClientSettings(getConservUrl());
			ConservClient cc = new ConservClient(settings);
			return cc;
		}

	}

	public static class Spring31Custom extends AbstractConfig {

		@PostConstruct
		public void postConstruct() {
			ConservPropertiesSource source = new ConservPropertiesSource(ConservClient(), "test", "example", "properties");
			environment.getPropertySources().addFirst(source);
		}

		@Autowired
		private ConfigurableEnvironment environment;

		public String getEnvProperty(String name) {
			return environment.getProperty(name);
		}

		@Bean
		public static ConservPropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			ConservClient client = ConservClient();
			ConservPropertyPlaceholderConfigurer configurer = new ConservPropertyPlaceholderConfigurer(client);
			configurer.setLocation(new FileSystemResource("/test/example/properties"));
			return configurer;
		}

	}

	//@PropertySource("http://www.google.com/xzncbzmnxcb")
	public static class Spring31Basic extends AbstractConfig {

		@PostConstruct
		public void postConstruct() {
			ConservPropertiesSource source = new ConservPropertiesSource(ConservClient(), "test", "example", "properties");
			environment.getPropertySources().addFirst(source);
		}

		@Autowired
		private ConfigurableEnvironment environment;

		public String getEvnProperty(String name) {
			return environment.getProperty(name);
		}

		/**
		 * Standard Spring 3.1 PropertySourcesPlaceholderConfigurer - feeds @Value
		 */
		@Bean
		public static PropertySourcesPlaceholderConfigurer sourcePlaceholderConfigurer() throws IOException {
			PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
			configurer.setLocation(new UrlResource(getConservUrl()));
			return configurer;
		}
	}

	public static class Spring30Custom extends AbstractConfig {

		@Bean
		public static ConservClient ConservClient() {
			ClientSettings settings = new ClientSettings(getConservUrl());
			ConservClient cc = new ConservClient(settings);
			return cc;
		}

		/**
		 * Custom Spring 2.0-3.0 ConservPropertyPlaceholderConfigurer - feeds @Value
		 */
		@Bean
		public static ConservPropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			ConservClient client = ConservClient();
			ConservPropertyPlaceholderConfigurer configurer = new ConservPropertyPlaceholderConfigurer(client);
			configurer.setLocation(new FileSystemResource("/test/example/properties"));
			return configurer;
		}

	}

	public static class Spring30Basic extends AbstractConfig {

		/**
		 * Standard Spring 2.0-3.0 PropertyPlaceholderConfigurer - feeds @Value
		 */
		@Bean
		public static PropertyPlaceholderConfigurer placeholderConfigurer() throws IOException {
			PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
			configurer.setLocation(new UrlResource(getConservUrl()));
			return configurer;
		}

	}

}
