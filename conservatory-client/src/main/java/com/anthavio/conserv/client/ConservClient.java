package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.anthavio.conserv.model.Configuration;

/**
 * 
 * GET http://somewhere.com:8080/conserve/configuration/{application}/{environment}
 * 
 * @author martin.vanek
 *
 */
public class ConservClient {

	private final URL url;

	private final String username;

	private final String password;

	public ConservClient(String url, String username, String password) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException mux) {
			throw new IllegalArgumentException("Invalid url: " + url, mux);
		}
		this.username = username;
		this.password = password;
	}

	public ConservClient(String url) {
		this(url, null, null);
	}

	public Configuration getConfiguration(String application, String environment) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(1000);
		connection.setReadTimeout(3000);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept-Charset", "utf-8");
		connection.setRequestProperty("Accept", "application/json"); //"application/json" needs Jackson

		if (username != null) {
			byte[] bytes = (username + ":" + password).getBytes(Charset.forName("utf-8"));
			String encoded = new String(Base64.encodeBase64(bytes, false));
			connection.setRequestProperty("Authorization", "Basic " + encoded);
		}

		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			//TODO optionaly log connection.getErrorStream();
			throw new IOException("Response " + responseCode + " " + connection.getResponseMessage());
		}

		InputStream responseStream = connection.getInputStream();
		Map<String, List<String>> responseHeaders = connection.getHeaderFields();
		String[] contentType = getContentType(responseHeaders);
		Configuration configuration;
		if (contentType[0].contains("xml")) {
			configuration = new JaxbDecoder().decode(responseStream);
		} else {
			configuration = new JacksonDecoder().decode(responseStream);
		}
		return configuration;
	}

	/**
	 * @return [0] mimeType, [1] encoding
	 */
	private String[] getContentType(Map<String, List<String>> responseHeaders) {
		String[] retval = new String[] { "application/xml", "utf-8" }; //defaults init
		List<String> headers = responseHeaders.get("Content-Type");
		if (headers != null && headers.size() != 0) {
			String header = headers.get(0);
			int idxSemCol = header.indexOf(';');
			if (idxSemCol != -1) {
				retval[0] = header.substring(0, idxSemCol);
				retval[1] = header.substring(idxSemCol + 1);
			} else {
				retval[0] = header;
			}
		}

		return retval;
	}

	public static interface ConfigurationDecorer {

		Configuration decode(InputStream stream) throws IOException;
	}
}
