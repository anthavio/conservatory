package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ConservLoader {

	public String[] load(URL url, ClientSettings settings) throws IOException;
}
