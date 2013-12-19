package net.anthavio.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.anthavio.discovery.StandardFinders.ClasspathFinder;
import net.anthavio.discovery.StandardFinders.EnvironmentValiableFinder;
import net.anthavio.discovery.StandardFinders.FilesystemFinder;
import net.anthavio.discovery.StandardFinders.ServiceLoaderFinder;
import net.anthavio.discovery.StandardFinders.SystemPropertiesFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author martin.vanek
 *
 */
public class PropertiesDiscovery {

	static final Logger logger = LoggerFactory.getLogger(PropertiesFinder.class);

	public static PropertiesDiscovery Builder() {
		return new PropertiesDiscovery();
	}

	private boolean exceptionOnNotFound = false;

	private boolean merge = false;

	private List<PropertiesFinder> finders = new ArrayList<PropertiesFinder>();

	public PropertiesDiscovery system(String propertyName) {
		add(new SystemPropertiesFinder(propertyName));
		return this;
	}

	public PropertiesDiscovery environment(String propertyName) {
		add(new EnvironmentValiableFinder(propertyName));
		return this;
	}

	public PropertiesDiscovery classpath(String resource) {
		add(new ClasspathFinder(resource, null));
		return this;
	}

	public PropertiesDiscovery classpath(String resource, Class<?> caller) {
		add(new ClasspathFinder(resource, caller));
		return this;
	}

	public PropertiesDiscovery filesystem(String fileNameSystemProperty) {
		add(new FilesystemFinder(fileNameSystemProperty));
		return this;
	}

	public PropertiesDiscovery serviceLoader() {
		add(new ServiceLoaderFinder());
		return this;
	}

	public PropertiesDiscovery add(PropertiesFinder finder) {
		if (finder == null) {
			throw new IllegalArgumentException("Null finder");
		}
		finders.add(finder);
		return this;
	}

	public Result discover(boolean exception) {
		this.exceptionOnNotFound = exception;
		return discover();
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
		if (exceptionOnNotFound) {
			throw new DiscoveryException("Properties not discovered using: " + finders);
		}
		return null;
	}

	public <T> T build(Assembler<T> assembler) {
		Result<T> finding = discover();
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

	public static class Result<T> {

		private final T source;

		private final Properties properties;

		public Result(T source, Properties properties) {
			if (source == null) {
				throw new IllegalArgumentException("null source");
			}
			this.source = source;
			if (properties == null) {
				throw new IllegalArgumentException("null properties");
			}
			this.properties = properties;
		}

		public T getSource() {
			return source;
		}

		public Properties getProperties() {
			return properties;
		}
	}

}
