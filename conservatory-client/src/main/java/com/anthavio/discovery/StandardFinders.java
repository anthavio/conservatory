package com.anthavio.discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.anthavio.discovery.DiscoveryBuilder.DiscoveryException;
import com.anthavio.discovery.DiscoveryBuilder.Finding;
import com.anthavio.discovery.DiscoveryBuilder.PropertiesFinder;

/**
 * 
 * @author martin.vanek
 *
 */
public class StandardFinders {

	public static class SystemFinder implements PropertiesFinder {

		private final String propertyName;

		public SystemFinder(String propertyName) {
			if (propertyName == null || propertyName.length() == 0) {
				throw new IllegalArgumentException("Blank property name");
			}
			this.propertyName = propertyName;
		}

		@Override
		public Finding find() {
			String property = System.getProperty(propertyName);
			if (property != null) {
				return new Finding("[System Properties]", System.getProperties());
			} else {
				return null;
			}
		}

	}

	public static class EnvironmentFinder implements PropertiesFinder {

		private final String propertyName;

		public EnvironmentFinder(String propertyName) {
			if (propertyName == null || propertyName.length() == 0) {
				throw new IllegalArgumentException("Blank property name");
			}
			this.propertyName = propertyName;
		}

		@Override
		public Finding find() {
			String property = System.getenv(propertyName);
			if (property != null) {
				Map<String, String> envars = System.getenv();
				Properties properties = new Properties();
				for (Entry<String, String> entry : envars.entrySet()) {
					properties.put(entry.getKey(), entry.getValue());
				}
				return new Finding("[Environment Variables]", System.getProperties());
			} else {
				return null;
			}
		}

	}

	public static class FilepathFinder implements PropertiesFinder {

		private final String fileNameSystemProperty;

		public FilepathFinder(String fileNameSystemProperty) {
			if (fileNameSystemProperty == null || fileNameSystemProperty.length() == 0) {
				throw new IllegalArgumentException("Blank system property name");
			}
			this.fileNameSystemProperty = fileNameSystemProperty;
		}

		@Override
		public Finding find() {
			String fileName = System.getProperty(fileNameSystemProperty);
			if (fileName != null) {
				File file = new File(fileName);
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
				return new Finding(file, properties);
			} else {
				return null;
			}
		}

	}

	public static class ClasspathFinder implements PropertiesFinder {

		private final ClassLoader classLoader;

		private final String resource;

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
		public Finding find() {
			URL url = classLoader.getResource(resource);
			if (url != null) {
				Properties properties;
				try {
					properties = load(url.openStream());
				} catch (IOException iox) {
					throw new DiscoveryException("Properties load failed from classpath resource " + url, iox);
				}
				return new Finding(url, properties);
			} else {
				return null;
			}
		}
	}

	public static class UrlFinder implements PropertiesFinder {

		private final String urlSystemProperty;

		public UrlFinder(String urlSystemProperty) {
			if (urlSystemProperty == null || urlSystemProperty.length() == 0) {
				throw new IllegalArgumentException("Blank system property name");
			}
			this.urlSystemProperty = urlSystemProperty;
		}

		@Override
		public Finding find() {
			String urlString = System.getProperty(urlSystemProperty);
			if (urlString != null) {
				try {
					URL url = new URL(urlString);
					Properties properties = load(url.openStream());
					return new Finding(url, properties);
				} catch (IOException iox) {
					throw new DiscoveryException("Properties load failed from url" + urlString, iox);
				}
			} else {
				return null;
			}

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
