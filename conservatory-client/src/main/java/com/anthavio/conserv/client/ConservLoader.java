package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ConservLoader {

	public LoadResult load(URL url, ClientSettings settings) throws IOException;

	public static class LoadResult {

		private String mimeType;

		private String charset;

		private String content;

		public LoadResult(String mimeType, String charset, String content) {
			this.mimeType = mimeType;
			this.charset = charset;
			this.content = content;
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
