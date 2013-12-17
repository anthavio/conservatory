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
public class Discovery {

	static final Logger logger = LoggerFactory.getLogger(PropertiesFinder.class);

	public static Discovery Builder() {
		return new Discovery();
	}

	private boolean merge = false;

	private List<PropertiesFinder> finders = new ArrayList<PropertiesFinder>();

	public Discovery system(String propertyName) {
		add(new SystemPropertiesFinder(propertyName));
		return this;
	}

	public Discovery environment(String propertyName) {
		add(new EnvironmentValiableFinder(propertyName));
		return this;
	}

	public Discovery classpath(String resource) {
		add(new ClasspathFinder(resource, null));
		return this;
	}

	public Discovery classpath(String resource, Class<?> caller) {
		add(new ClasspathFinder(resource, caller));
		return this;
	}

	public Discovery filepath(String fileNameSystemProperty) {
		add(new FilesystemFinder(fileNameSystemProperty));
		return this;
	}

	public Discovery add(PropertiesFinder finder) {
		if (finder == null) {
			throw new IllegalArgumentException("Null finder");
		}
		finders.add(finder);
		return this;
	}

	public Result discover() {
		for (PropertiesFinder finder : finders) {
			Result finding = finder.find();
			if (finding != null) {
				logger.info("Located with " + finder);
				//TODO merge properties... 
				return finding;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Nothing with " + finder);
				}
			}
		}
		return null;
	}

	public <T> T build(Assembler<T> assembler) {
		Result finding = discover();
		if (finding != null) {
			return assembler.assemble(finding.properties);
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

	public static class Result {

		private final Object source;

		private final Properties properties;

		public Result(Object source, Properties properties) {
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

		public Result find();
	}
}
