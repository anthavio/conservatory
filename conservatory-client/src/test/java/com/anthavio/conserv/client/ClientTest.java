package com.anthavio.conserv.client;

import com.anthavio.conserv.model.Config;

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
			Config configuration = client.getConfiguration("live", "example", "properties");
			System.out.println(configuration);
		} catch (Exception x) {
			x.printStackTrace();
		}

	}
}
