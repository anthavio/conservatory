package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;

import com.anthavio.conserv.model.Configuration;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ConfigParser {

	public static enum Format {
		XML, JSON;
	}

	public Format getFormat();

	public Configuration parse(Reader reader) throws IOException;
}
