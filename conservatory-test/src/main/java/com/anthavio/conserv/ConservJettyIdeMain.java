package com.anthavio.conserv;

import com.anthavio.jetty.JettyWrapper;

/**
 * 
 * @author martin.vanek
 *
 */
public class ConservJettyIdeMain {

	public static void main(String[] args) {
		new JettyWrapper("src/main/jetty", 9090).start();
	}
}
