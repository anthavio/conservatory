package net.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import net.anthavio.conserv.model.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	public Config parse(Reader reader) throws IOException {
		return mapper.readValue(reader, Config.class);
	}

	/**
	 * Well, for testing mostly...
	 */
	public String marshall(Config config) {
		try {
			StringWriter sw = new StringWriter();
			mapper.writeValue(sw, config);
			return sw.toString();
		} catch (JsonProcessingException jmx) {
			throw new IllegalStateException("This should never happen", jmx);
		} catch (IOException iox) {
			throw new IllegalStateException("This should never happen", iox);
		}
	}

}