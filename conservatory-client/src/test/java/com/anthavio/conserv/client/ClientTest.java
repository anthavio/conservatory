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
			ClientSettings settings = new ClientSettings("http://localhost:9090/conserv/api/config");
			//settings.setConfigParser(new JaxbConfigParser());
			ConservClient client = new ConservClient(settings);
			Configuration configuration = client.getConfiguration("live", "example", "properties");
			System.out.println(configuration);
		} catch (Exception x) {
			x.printStackTrace();
		}

	}
}
