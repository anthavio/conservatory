package com.anthavio.conserv.client;

import java.io.IOException;
import java.io.Reader;

import com.anthavio.conserv.model.Config;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ConfigParser {

	public static enum Format {
		XML('<', "application/xml"), JSON('{', "application/json");

		private final char fchar;

		private final String accept;

		private Format(char fchar, String accept) {
			this.fchar = fchar;
			this.accept = accept;
		}

		public boolean supports(char fchar) {
			return this.fchar == fchar;
		}

		public String getAcceptHeader() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public Format getFormat();

	public Config parse(Reader reader) throws IOException;
}
