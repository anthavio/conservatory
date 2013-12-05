package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;

import com.anthavio.conserv.model.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * For environments where default JAXB is not an option - Android devices for example
 * 
 * @author martin.vanek
 *
 */
public class Jackson2ConfigParser implements ConfigParser {

	private final ObjectMapper mapper;

	public Jackson2ConfigParser() {
		this(new ObjectMapper());
	}

	public Jackson2ConfigParser(ObjectMapper mapper) {
		if (mapper == null) {
			throw new IllegalArgumentException("Null Jackson Mapper");
		}
		this.mapper = mapper;
	}

	public Format getFormat() {
		return Format.JSON;
	}

	@Override
	public Configuration parse(Reader reader) throws IOException {
		return mapper.readValue(reader, Configuration.class);
	}

}