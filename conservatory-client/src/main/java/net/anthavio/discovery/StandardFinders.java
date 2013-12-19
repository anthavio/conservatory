package net.anthavio.discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import net.anthavio.discovery.PropertiesDiscovery.DiscoveryException;
import net.anthavio.discovery.PropertiesDiscovery.Result;


/**
 * 
 * @author martin.vanek
 *
 */
public class StandardFinders {

	public static class SystemPropertiesFinder implements PropertiesFinder<String> {

		private final String propertyName;

		public SystemPropertiesFinder(String propertyName) {
			if (propertyName == null || propertyName.length() == 0) {
				throw new IllegalArgumentException("Blank property name");
			}
			this.propertyName = propertyName;
		}

		@Override
		public Result<String> find() {
			String property = System.getProperty(propertyName);
			if (property != null) {
				return new Result<String>("[System Properties]", System.getProperties());
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + propertyName;
		}

	}

	public static class EnvironmentValiableFinder implements PropertiesFinder<String> {

		private final String variableName;

		public EnvironmentValiableFinder(String variableName) {
			if (variableName == null || variableName.length() == 0) {
				throw new IllegalArgumentException("Blank variable name");
			}
			this.variableName = variableName;
		}

		@Override
		public Result<String> find() {
			String property = System.getenv(variableName);
			if (property != null) {
				Map<String, String> envars = System.getenv();
				Properties properties = new Properties();
				for (Entry<String, String> entry : envars.entrySet()) {
					properties.put(entry.getKey(), entry.getValue());
				}
				return new Result<String>("[Environment Variables]", System.getProperties());
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + variableName;
		}
	}

	public static class FilesystemFinder implements PropertiesFinder<File> {

		private final String fileNameSystemProperty;

		private File file;

		public FilesystemFinder(String fileNameSystemProperty) {
			if (fileNameSystemProperty == null || fileNameSystemProperty.length() == 0) {
				throw new IllegalArgumentException("Blank system property name");
			}
			this.fileNameSystemProperty = fileNameSystemProperty;
		}

		@Override
		public Result<File> find() {
			String fileName = System.getProperty(fileNameSystemProperty);
			if (fileName != null) {
				file = new File(fileName);
				if (!file.exists()) {
					throw new DiscoveryException("Invalid system property '" + fileNameSystemProperty
							+ "'. File does not exist: " + file);
				}
				if (!file.canRead()) {
					throw new DiscoveryException("Invalid system property '" + fileNameSystemProperty + "'. File not readable:"
							+ file);
				}
				Properties properties;
				try {
					properties = load(new FileInputStream(file));
				} catch (IOException iox) {
					throw new DiscoveryException("Properties load failed from file " + file, iox);
				}
				return new Result<File>(file, properties);
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + fileNameSystemProperty + " " + file;
		}

	}

	public static class ClasspathFinder implements PropertiesFinder<URL> {

		private final ClassLoader classLoader;

		private final String resource;

		private transient URL url;

		/*
		public ClasspathItemBuilder(String resource) {
			this(null, resource);
		}
		*/

		public ClasspathFinder(String resource, Class<?> caller) {
			if (resource == null || resource.length() == 0) {
				throw new IllegalArgumentException("Blank resource");
			}
			this.resource = resource;

			if (caller != null) {
				this.classLoader = caller.getClassLoader();
			} else {
				ClassLoader loader = getContextClassLoader();
				if (loader == null) {
					loader = ClassLoader.getSystemClassLoader();
				}
				this.classLoader = loader;
			}
		}

		@Override
		public Result<URL> find() {
			url = classLoader.getResource(resource);
			if (url != null) {
				Properties properties;
				try {
					properties = load(url.openStream());
				} catch (IOException iox) {
					throw new DiscoveryException("Properties load failed from classpath resource " + url, iox);
				}
				return new Result<URL>(url, properties);
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + resource + " " + url;
		}
	}

	public static class UrlFinder implements PropertiesFinder<URL> {

		private final String urlSystemProperty;

		private transient URL url;

		public UrlFinder(String urlSystemProperty) {
			if (urlSystemProperty == null || urlSystemProperty.length() == 0) {
				throw new IllegalArgumentException("Blank system property name");
			}
			this.urlSystemProperty = urlSystemProperty;
		}

		@Override
		public Result<URL> find() {
			String urlString = System.getProperty(urlSystemProperty);
			if (urlString != null) {
				try {
					url = new URL(urlString);
					Properties properties = load(url.openStream());
					return new Result<URL>(url, properties);
				} catch (IOException iox) {
					throw new DiscoveryException("Properties load failed from url" + urlString, iox);
				}
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + urlSystemProperty + " " + url;
		}
	}

	/**
	 * Makes java.util.ServiceLoader lookup to find PropertiesFinder interface implementation to load Properties
	 * 
	 * - Create class implementing com.anthavio.discovery.PropertiesFinder (example com.myapp.MyGreatPropertiesFinderImpl)
	 * - Create file 
	 * 		META-INF/services/com.anthavio.discovery.PropertiesFinder
	 *   Having single line
	 *   	com.myapp.MyGreatPropertiesFinderImpl
	 */
	public static class ServiceLoaderFinder implements PropertiesFinder {

		private transient PropertiesFinder finder;

		@Override
		public Result find() {
			ServiceLoader<PropertiesFinder> loader = ServiceLoader.load(PropertiesFinder.class);
			Iterator<PropertiesFinder> iterator = loader.iterator();
			if (iterator.hasNext()) {
				this.finder = iterator.next();
				return finder.find();
			}

			return null;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " " + finder;
		}
	}

	public static ClassLoader getContextClassLoader() {
		if (System.getSecurityManager() == null) {
			return Thread.currentThread().getContextClassLoader();
		} else {
			return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
				public java.lang.Object run() {
					return Thread.currentThread().getContextClassLoader();
				}
			});
		}
	}

	public static Properties load(InputStream stream) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(stream);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException iox) {
					//ignore this...
				}
			}
		}
		return properties;
	}

}
