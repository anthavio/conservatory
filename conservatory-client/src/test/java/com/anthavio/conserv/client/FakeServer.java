package com.anthavio.conserv.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

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

import com.anthavio.conserv.model.Config;
import com.anthavio.conserv.model.Property;

/**
 * 
 * @author martin.vanek
 *
 */
public class FakeServer extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Server jetty;

	private JaxbConfigParser jaxb = new JaxbConfigParser();

	private Jackson2ConfigParser json = new Jackson2ConfigParser();

	private Config config = TestData.getDefaultConfig();

	//private Format format = Format.PLAIN;

	private String errorMessage;

	public FakeServer(boolean authon) {
		jetty = setup(authon);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (errorMessage != null) {
			response.sendError(503, errorMessage);
			return;
		}

		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince == config.getCreatedAt().getTime()) {
			//dtModified = new Date(ifModifiedSince);
			//config.getCreatedAt().getTime()==ifModifiedSince;
			response.sendError(HttpURLConnection.HTTP_NOT_MODIFIED);
			if (true) {
				return;
			}
		}

		String accept = request.getHeader("Accept");
		if (accept == null) {
			accept = "text/plain";
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(accept + ";charset=utf-8");
		System.out.println("FakeServer - returning config as " + accept);

		String content;
		if (accept.indexOf("json") != -1) {
			content = json.marshall(config);
		} else if (accept.indexOf("xml") != -1) {
			content = jaxb.marshall(config);
		} else {
			StringBuilder sb = new StringBuilder();
			for (Property p : config.getProperties()) {
				sb.append(p.getName() + " = " + p.getValue()).append('\n');
			}
			content = sb.toString();
			//throw new IllegalStateException("What?!?! " + format);
		}
		response.getWriter().println(content);
	}

	/*
		public void setResponseConfig(Config config, Format format) {
			this.format = format;
			this.config = config;
		}

		public Format getFormat() {
			return format;
		}

		public void setFormat(Format format) {
			this.format = format;
		}
	*/
	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private Server setup(boolean authentication) {
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

		if (authentication) {
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
