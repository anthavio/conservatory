package com.anthavio.conserv.client;

import com.anthavio.conserv.model.Configuration;

/**
 * 
 * @author martin.vanek
 *
 */
public class ClientTest {

	public static void main(String[] args) {
		try {
			ConservClient client = new ConservClient("http://localhost:9090/conserv/config/live/naturenews/properties");
			Configuration configuration = client.getConfiguration("live", "naturenews", "properties");
			System.out.println(configuration);
		} catch (Exception x) {
			x.printStackTrace();
		}

	}
}
