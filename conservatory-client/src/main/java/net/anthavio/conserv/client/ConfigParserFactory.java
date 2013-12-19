package net.anthavio.conserv.client;

import java.lang.reflect.Method;

import net.anthavio.conserv.model.Config;

/**
 * 
 * @author martin.vanek
 *
 */
class ConfigParserFactory {

	public static ConfigParser build() {
		if (isJaxbPresent()) {
			return build("net.anthavio.conserv.client.JaxbConfigParser");
		}
		if (isJacksonPresent()) {
			return build("net.anthavio.conserv.client.Jackson2ConfigParser");
		}
		throw new IllegalStateException("Neither JAXB nor Jackson found");
	}

	private static ConfigParser build(String className) {
		Class<ConfigParser> clazz;
		try {
			clazz = (Class<ConfigParser>) Class.forName(className);
			return clazz.newInstance();
		} catch (Exception x) {
			throw new IllegalStateException("Failed to instantiate " + className, x);
		}
	}

	private static boolean isJacksonPresent() {
		try {
			Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
			return true;
		} catch (ClassNotFoundException cnfx) {
			return false;
		}
	}

	private static boolean isJaxbPresent() {
		try {
			Class<?> jaxbContext = Class.forName("javax.xml.bind.JAXBContext");
			Method newInstance = jaxbContext.getMethod("newInstance", Class[].class);
			newInstance.invoke(null, new Object[] { new Class[] { Config.class } });//reflection vararg
			return true;
		} catch (LinkageError le) {
		} catch (Exception x) {
		}
		return false;
	}
}
