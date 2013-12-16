package com.anthavio.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthavio.discovery.StandardFinders.ClasspathFinder;
import com.anthavio.discovery.StandardFinders.EnvironmentValiableFinder;
import com.anthavio.discovery.StandardFinders.FilesystemFinder;
import com.anthavio.discovery.StandardFinders.SystemPropertiesFinder;

/**
 * 
 * @author martin.vanek
 *
 */
public class DiscoveryBuilder {

	static final Logger logger = LoggerFactory.getLogger(PropertiesFinder.class);

	public static DiscoveryBuilder Builder() {
		return new DiscoveryBuilder();
	}

	private List<PropertiesFinder> finders = new ArrayList<PropertiesFinder>();

	public DiscoveryBuilder system(String propertyName) {
		add(new SystemPropertiesFinder(propertyName));
		return this;
	}

	public DiscoveryBuilder environment(String propertyName) {
		add(new EnvironmentValiableFinder(propertyName));
		return this;
	}

	public DiscoveryBuilder classpath(String resource) {
		add(new ClasspathFinder(resource, null));
		return this;
	}

	public DiscoveryBuilder classpath(String resource, Class<?> caller) {
		add(new ClasspathFinder(resource, caller));
		return this;
	}

	public DiscoveryBuilder filepath(String fileNameSystemProperty) {
		add(new FilesystemFinder(fileNameSystemProperty));
		return this;
	}

	public DiscoveryBuilder add(PropertiesFinder finder) {
		if (finder == null) {
			throw new IllegalArgumentException("Null finder");
		}
		finders.add(finder);
		return this;
	}

	public Properties discover() {
		for (PropertiesFinder discoverer : finders) {
			Finding finding = discoverer.find();
			if (finding != null) {
				logger.info("Discovered " + discoverer);
				return finding.properties;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Nothing in " + discoverer);
				}
			}
		}
		return null;
	}

	public <T> T build(Assembler<T> assembler) {
		Properties properties = discover();
		if (properties != null) {
			return assembler.assemble(properties);
		}
		return null;
	}

	public static interface Assembler<T> {

		public T assemble(Properties properties);
	}

	public static class DiscoveryException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public DiscoveryException(Throwable cause) {
			super(cause);
		}

		public DiscoveryException(String message) {
			super(message);
		}

		public DiscoveryException(String message, Exception cause) {
			super(message, cause);
		}
	}

	public static class Finding {

		private final Object source;

		private final Properties properties;

		public Finding(Object source, Properties properties) {
			if (source == null) {
				throw new IllegalArgumentException("null source");
			}
			this.source = source;
			if (properties == null) {
				throw new IllegalArgumentException("null properties");
			}
			this.properties = properties;
		}

		public Object getSource() {
			return source;
		}

		public Properties getProperties() {
			return properties;
		}
	}

	public static interface PropertiesFinder {

		public Finding find();
	}
}
