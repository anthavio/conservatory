package com.anthavio.conserv;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.anthavio.jetty.JettyWrapper;

/**
 * 
 * @author martin.vanek
 *
 */
public class ConservJettyIdeMain {

	public static void main(String[] args) {
		//WEB-INF/classes/logging.properties
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		new JettyWrapper("src/main/jetty", 9090).start();
	}
}
