package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * 
 * @author martin.vanek
 *
 */
public class JavaPropsParser implements ConfigParser {

	@Override
	public Format getFormat() {
		return Format.PLAIN;
	}

	@Override
	public Config parse(Reader reader) throws IOException {
		Properties props = new Properties();
		props.load(reader);
		List<Property> properties = new ArrayList<Property>(props.size());
		Set<String> names = props.stringPropertyNames();
		for (String name : names) {
			properties.add(new Property(name, props.getProperty(name)));
		}

		String environment = props.getProperty("conserv.environment");
		String application = props.getProperty("conserv.application");
		String resource = props.getProperty("conserv.resource");
		String createdAt = props.getProperty("conserv.createdAt");
		Date dtCreatedAt = null;
		if (createdAt != null) {
			try {
				dtCreatedAt = new SimpleDateFormat(Property.DATE_TIME_FORMAT).parse(createdAt);
			} catch (ParseException px) {
				//FIXME logger.warn("Failed to parse createdAt " + createdAt);
			}
		}

		return new Config(environment, application, resource, dtCreatedAt, properties);
	}

	/**
	 * Well, for testing mostly...
	 */
	public String marshall(Config config) {

		List<Property> properties = config.getProperties();
		Properties props = new Properties();
		for (Property property : properties) {
			props.put(property.getName(), property.getValue());
		}
		StringWriter sw = new StringWriter();
		try {
			props.store(sw, null);
		} catch (IOException iox) {
			throw new IllegalStateException("This should never happen", iox);
		}
		return sw.toString();

	}

}
