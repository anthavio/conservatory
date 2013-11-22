package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.InputStream;

import com.anthavio.conserv.client.ConservClient.ConfigurationDecorer;
import com.anthavio.conserv.model.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author martin.vanek
 *
 */
public class JacksonDecoder implements ConfigurationDecorer {

	ObjectMapper mapper = new ObjectMapper();

	public JacksonDecoder() {

	}

	@Override
	public Configuration decode(InputStream stream) throws IOException {
		return mapper.readValue(stream, Configuration.class);
	}

}
