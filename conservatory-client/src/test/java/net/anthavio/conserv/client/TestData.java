package net.anthavio.conserv.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.anthavio.conserv.model.Config;
import net.anthavio.conserv.model.Property;


/**
 * 
 * @author martin.vanek
 *
 */
public class TestData {

	public static Config getDefaultConfig() {
		List<Property> properties = new ArrayList<Property>();
		properties.add(new Property("string.property", "Some value", "Comment for string property"));
		properties.add(new Property("integer.property", 132456789, "Comment for integer property"));
		properties.add(new Property("date.property", new Date(2001, 11, 25), "Comment for date property"));
		properties.add(new Property("url.property", URI.create("http://test-www.example.com:8080/zxzxzx")));

		properties.add(new Property("conserv.environment", "test"));
		properties.add(new Property("conserv.application", "example"));
		properties.add(new Property("conserv.resource", "properties"));

		return new Config("test", "example", "properties", new Date(2013, 11, 25), properties);
	}

}
