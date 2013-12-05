package com.anthavio.conserv.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthavio.conserv.client.ConfigParser.Format;
import com.anthavio.conserv.model.Configuration;

/**
 * 
 * GET http://somewhere.com:8080/conserve/config/{environment}/{application}/{resource}
 * 
 * @author martin.vanek
 *
 */
public class ConservClient {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ClientSettings settings;

	public ConservClient(String serverUrl) {
		this(new ClientSettings(serverUrl));
	}

	public ConservClient(ClientSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("Null settings");
		}
		this.settings = settings;
		if (settings.getConfigParser() == null) {
			settings.setConfigParser(new JaxbConfigParser());
		}
	}

	public Configuration getConfiguration(String environment, String application, String resource) {
		if (environment == null || environment.length() == 0) {
			throw new IllegalArgumentException("Environment is blank: " + environment);
		}
		if (application == null || application.length() == 0) {
			throw new IllegalArgumentException("Application is blank: " + application);
		}
		if (resource == null || resource.length() == 0) {
			throw new IllegalArgumentException("Resource is blank: " + resource);
		}
		String configPath = environment + "/" + application + "/" + resource;

		String fullUrl = settings.getServerUrl().toString();
		if (!fullUrl.endsWith("/")) {
			fullUrl += "/";
		}
		fullUrl += configPath;
		URL resourceUrl;
		try {
			//may happen when environment/application/resource contains something illegal
			resourceUrl = new URL(fullUrl);
		} catch (MalformedURLException mux) {
			throw new IllegalStateException("Malformed resource url: " + configPath, mux);
		}

		String filename = configPath.replace('/', '-');
		File fileConfig = new File(settings.getConfigDirectory(), filename);
		try {
			String[] aResponse = load(resourceUrl);
			Configuration config = parse(aResponse[0], aResponse[1]);
			save(fileConfig, aResponse[0]);
			return config;
		} catch (Exception x) {
			if (settings.getConfigKeeping() && fileConfig.exists()) {
				logger.error("Configuration server load failed", x);
				try {
					Configuration config = load(fileConfig);
					logger.info("Configuration loaded from " + fileConfig);
					return config;
				} catch (Exception x2) {
					logger.error("Configuration local load failed", x2);
					throw new IllegalStateException("Configuration server and local load failed", x);
				}
			} else {
				throw new IllegalStateException("Configuration server load failed", x); //TODO create better Exception
			}
		}
	}

	private Configuration load(File file) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
			return parse(sb.toString(), "xml");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Never ever throw any exception from this method 
	 */
	private void save(File file, String content) {
		try {
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
				writer.write(content); //TODO write content in more chunks...
				writer.close();
				logger.info("Configuration saved into " + file);
			} catch (IOException iox) {
				logger.warn("Error writing " + file, iox);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException iox) {
						logger.warn("Error closing writer for " + file, iox);
					}
				}
			}
		} catch (Exception x) {
			logger.warn("Error saving " + file, x);
		}
	}

	private Configuration parse(String content, String mimeType) throws IOException {
		ConfigParser parser = settings.getConfigParser();
		char fchar = getFirstCharacter(content);
		if (parser.getFormat() == Format.XML) {
			if (fchar == '<') {
				return parser.parse(new StringReader(content));
			} else {
				throw new IllegalStateException("Expected format is " + parser.getFormat() + " but content starts with "
						+ fchar);
			}
		} else if (parser.getFormat() == Format.JSON) {
			if (fchar == '{') {
				return parser.parse(new StringReader(content));
			} else {
				throw new IllegalStateException("Expected format is " + parser.getFormat() + " but content starts with "
						+ fchar);
			}
		} else {
			throw new IllegalStateException("Unsupported format " + parser.getFormat());
		}

	}

	/**
	 * Get first non whitespace character from content
	 */
	private char getFirstCharacter(String content) {
		for (int i = 0; i < content.length(); ++i) {
			char c = content.charAt(i);
			if (!Character.isWhitespace(c)) {
				return c;
			}
		}
		return ' ';
	}

	private String[] load(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		HttpURLConnection.setFollowRedirects(settings.getFollowRedirects());
		connection.setConnectTimeout(settings.getConnectTimeout());
		connection.setReadTimeout(settings.getReadTimeout());
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept-Charset", "utf-8");

		switch (settings.getConfigParser().getFormat()) {
		case XML:
			connection.setRequestProperty("Accept", "application/xml"); //needs jaxb avaliable
			break;
		case JSON:
			connection.setRequestProperty("Accept", "application/json"); //needs jackson 2 on classpath
			break;
		default:
			throw new IllegalStateException("Unsuported format " + settings.getConfigParser().getFormat());
		}

		if (settings.getUsername() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Setting BASIC authentication credentials for " + settings.getUsername());
			}
			byte[] bytes = (settings.getUsername() + ":" + settings.getPassword()).getBytes(Charset.forName("utf-8"));
			String encoded = new String(Base64.encodeBase64(bytes, false));
			connection.setRequestProperty("Authorization", "Basic " + encoded);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Requesting: " + url);
		}
		int responseCode = connection.getResponseCode(); //server interaction happens right here

		Map<String, List<String>> responseHeaders = connection.getHeaderFields();
		String[] contentType = getContentType(responseHeaders);

		if (responseCode != 200) {
			String errorContent = consume(connection.getErrorStream(), contentType[1]);
			if (logger.isDebugEnabled()) {
				logger.debug("Error:\n" + errorContent);
			}
			throw new IOException("Response " + responseCode + " " + connection.getResponseMessage());
		}

		String responseContent = consume(connection.getInputStream(), contentType[1]);
		if (logger.isDebugEnabled()) {
			//Just a response message on debug level 
			logger.debug("Response: " + responseCode + " Message: " + connection.getResponseMessage());
		} else if (logger.isTraceEnabled()) {
			//Log response body only on trace level
			logger.trace("Response:\n" + responseContent);
		}
		return new String[] { responseContent, contentType[0], contentType[1] };
	}

	private String consume(InputStream stream, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.forName(encoding)));
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line).append('\n');
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	/**
	 * Content-Type: application/xml; charset=utf-8
	 * 
	 * @return [0] mimeType, [1] encoding
	 */
	private String[] getContentType(Map<String, List<String>> headers) {
		String[] retval = new String[] { "application/xml", "utf-8" }; //expected defaults
		List<String> values = headers.get("Content-Type");
		if (values != null && values.size() != 0) {
			String value = values.get(0);
			int idxSemCol = value.indexOf(';');
			if (idxSemCol != -1) {
				retval[0] = value.substring(0, idxSemCol);
				int idxEquals = value.indexOf('=', idxSemCol);
				if (idxEquals != -1) {
					retval[1] = value.substring(idxEquals + 1).trim();
				} else {
					retval[1] = value.substring(idxSemCol + 1).trim(); //allows malformed format -> application/xml;utf-8
				}
			} else {
				//charset part not present -> keep default encoding in retval[1]
				retval[0] = value;
			}
		}
		return retval;
	}
}
