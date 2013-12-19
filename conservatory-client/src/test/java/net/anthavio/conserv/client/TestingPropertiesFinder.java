package net.anthavio.conserv.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.anthavio.conserv.client.ConservInitException;
import net.anthavio.discovery.PropertiesFinder;
import net.anthavio.discovery.PropertiesDiscovery.Result;


/**
 * 
 * @author martin.vanek
 *
 */
public class TestingPropertiesFinder implements PropertiesFinder<Object> {

	public static final String SYSPROP_NAME = "custom.locator.file";

	public TestingPropertiesFinder() {
		//must have default constructor
	}

	@Override
	public Result<Object> find() {
		String fileName = System.getProperty(SYSPROP_NAME);
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(fileName)));
		} catch (IOException iox) {
			throw new ConservInitException("Cannot load " + fileName, iox);
		}
		return new Result<Object>(fileName, properties);
	}
}
