package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ConservLoader {

	public LoadResult load(URL url, ClientSettings settings, Date lastModified) throws IOException;

	public static class LoadResult {

		private int httpCode;

		private String mimeType;

		private String charset;

		private String content;

		public LoadResult(int httpCode, String mimeType, String charset, String content) {
			this.httpCode = httpCode;
			this.mimeType = mimeType;
			this.charset = charset;
			this.content = content;
		}

		public int getHttpCode() {
			return httpCode;
		}

		public String getMimeType() {
			return mimeType;
		}

		public String getCharset() {
			return charset;
		}

		public String getContent() {
			return content;
		}

	}
}
