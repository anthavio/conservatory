package com.anthavio.conserv.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.anthavio.discovery.PropertiesDiscovery.Result;
import com.anthavio.discovery.PropertiesFinder;

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
