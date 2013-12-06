package com.anthavio.conserv.client;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.anthavio.conserv.model.Config;

/**
 * 
 * @author martin.vanek
 *
 */
public class ClientTest {

	private FakeServer server = new FakeServer(true);

	@BeforeClass
	public void beforeClass() {
		server.start();
	}

	@AfterClass
	public void afterClass() {
		//server.stop();
	}

	@Test
	public void test() {
		ClientSettings settings = new ClientSettings("http://localhost:" + server.getHttpPort() + "/conserv/api/config");
		//ConservLoader loader = new FakeConservLoader();
		//settings.setConservLoader(loader);
		//settings.setConfigParser(new Jackson2ConfigParser());
		settings.setCredentials("username", "password");
		ConservClient client = new ConservClient(settings);
		Config config = client.getConfig("x", "y", "z");
		System.out.println(config);

	}

	public static void main(String[] args) {
		try {
			ClientSettings settings = new ClientSettings("http://localhost:9090/conserv/api/config");
			//settings.setConfigParser(new Jackson2ConfigParser());
			ConservClient client = new ConservClient(settings);
			Config configuration = client.getConfig("live", "example", "properties");
			System.out.println(configuration);
		} catch (Exception x) {
			x.printStackTrace();
		}

	}
}
