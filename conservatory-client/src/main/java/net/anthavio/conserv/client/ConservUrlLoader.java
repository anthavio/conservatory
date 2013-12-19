package net.anthavio.conserv.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author martin.vanek
 *
 */
public class ConservUrlLoader implements ConservLoader {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public LoadResult load(URL url, ClientSettings settings, Date lastModified) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		HttpURLConnection.setFollowRedirects(settings.getFollowRedirects());
		connection.setConnectTimeout(settings.getConnectTimeout());
		connection.setReadTimeout(settings.getReadTimeout());
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept-Charset", "utf-8");
		connection.setRequestProperty("Accept", settings.getConfigParser().getFormat().getMimeType());
		if (lastModified != null) {
			connection.setIfModifiedSince(lastModified.getTime());
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
			logger.debug("Request " + settings.getConfigParser().getFormat() + " from " + url);
		}
		int responseCode = connection.getResponseCode(); //server interaction happens right here

		Map<String, List<String>> responseHeaders = connection.getHeaderFields();
		String[] contentType = getContentType(responseHeaders);

		if (logger.isDebugEnabled()) {
			//Log just a response message on debug level 
			logger.debug("Response: " + responseCode + ", Message: " + connection.getResponseMessage() + ", Type: "
					+ contentType[0]);
		}

		if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
			return new LoadResult(responseCode, null, null, null); //nothing else...
		}

		if (responseCode != HttpURLConnection.HTTP_OK) {
			InputStream stream = connection.getErrorStream();
			if (stream == null) {
				stream = connection.getInputStream();
			}
			String errorContent = consume(stream, contentType[1]);
			if (logger.isDebugEnabled()) {
				logger.debug("Error:\n" + errorContent);
			}
			throw new IOException("Response " + responseCode + " " + connection.getResponseMessage()); //XXX better exception
		}

		String responseContent = consume(connection.getInputStream(), contentType[1]);
		if (logger.isTraceEnabled()) {
			//Log response body only on trace level
			logger.trace("Response:\n" + responseContent);
		}
		return new LoadResult(responseCode, contentType[0], contentType[1], responseContent);
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
