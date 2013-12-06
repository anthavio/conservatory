package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.anthavio.conserv.client.ConfigParser.Format;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * To simplify client tests without config server
 * 
 * @author martin.vanek
 *
 */
public class FakeConservLoader implements ConservLoader {

	private JaxbConfigParser jaxb = new JaxbConfigParser();

	private Jackson2ConfigParser json = new Jackson2ConfigParser();

	private Format format;

	private Config config;

	private String charset = "utf-8";

	private RuntimeException exception;

	public FakeConservLoader() {
		List<Property> properties = new ArrayList<Property>();
		properties.add(new Property("string.property", "Some value"));
		properties.add(new Property("integer.property", 132456789));
		properties.add(new Property("date.property", new Date()));
		properties.add(new Property("url.property", URI.create("http://test-www.example.com:8080/zxzxzx")));
		this.config = new Config("example", "test", new Date(), properties);
		this.format = Format.XML;
	}

	@Override
	public LoadResult load(URL url, ClientSettings settings) throws IOException {
		if (exception != null) {
			throw exception;
		}
		String content;
		if (format == Format.JSON) {
			content = json.marshall(config);
		} else if (format == Format.XML) {
			content = jaxb.marshall(config);
		} else {
			throw new IllegalStateException("What?!?! " + format);
		}

		return new LoadResult(format.getMimeType(), charset, content);
	}

	public void setResponseConfig(Config config, Format format) {
		this.format = format;
		this.config = config;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public RuntimeException getException() {
		return exception;
	}

	public void setException(RuntimeException exception) {
		this.exception = exception;
	}
}
