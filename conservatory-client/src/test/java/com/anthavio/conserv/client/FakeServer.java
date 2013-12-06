package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

import com.anthavio.conserv.client.ConfigParser.Format;
import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * 
 * @author martin.vanek
 *
 */
public class FakeServer extends HttpServlet {

	private Server jetty;

	private JaxbConfigParser jaxb = new JaxbConfigParser();

	private Jackson2ConfigParser json = new Jackson2ConfigParser();

	private Format format;

	private Config config;

	private String errorMessage;

	public FakeServer(boolean authon) {
		jetty = setup(authon);
		List<Property> properties = new ArrayList<Property>();
		properties.add(new Property("string.property", "Some value"));
		properties.add(new Property("integer.property", 132456789));
		properties.add(new Property("date.property", new Date()));
		properties.add(new Property("url.property", URI.create("http://test-www.example.com:8080/zxzxzx")));
		this.config = new Config("example", "test", new Date(), properties);
		this.format = Format.XML;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (errorMessage != null) {
			response.sendError(503, errorMessage);
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(format.getMimeType() + ";charset=utf-8");
		String content;
		if (format == Format.JSON) {
			content = json.marshall(config);
		} else if (format == Format.XML) {
			content = jaxb.marshall(config);
		} else {
			throw new IllegalStateException("What?!?! " + format);
		}
		response.getWriter().println(content);
	}

	public void setResponseConfig(Config config, Format format) {
		this.format = format;
		this.config = config;
	}

	private Server setup(boolean authon) {
		// http://www.eclipse.org/jetty/documentation/current/embedded-examples.html
		// https://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
		Server server = new Server(0);

		HashLoginService loginService = new HashLoginService("MyRealm", "src/test/resources/realm.properties");
		loginService.putUser("username", new Password("password"), new String[] { "user_role", "adnin_role" });

		ConstraintSecurityHandler security = new ConstraintSecurityHandler();

		Constraint constraint = new Constraint();
		constraint.setName("auth");
		constraint.setAuthenticate(true);
		constraint.setRoles(new String[] { "user_role", "admin_role" });

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.setPathSpec("/*");
		mapping.setConstraint(constraint);

		security.setConstraintMappings(Collections.singletonList(mapping));
		security.setAuthenticator(new BasicAuthenticator());
		security.setLoginService(loginService);

		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(new ServletHolder(this), "/*");
		server.setHandler(handler);

		if (authon) {
			security.setHandler(handler); //chain the shit...
			server.setHandler(security);
		} else {
			server.setHandler(handler);
		}

		server.addBean(loginService);

		return server;
	}

	public int getHttpPort() {
		if (jetty.isRunning()) {
			return jetty.getConnectors()[0].getLocalPort();
		} else {
			throw new IllegalStateException("Server is not started");
		}
	}

	public void start() {
		if (jetty.isStarted()) {
			throw new IllegalStateException("Server is already started");
		}
		try {
			jetty.start();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}

	public void stop() {
		if (jetty.isRunning()) {
			throw new IllegalStateException("Server not running");
		}
		try {
			jetty.stop();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}

}
