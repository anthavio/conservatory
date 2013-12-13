package com.anthavio.conserv.client;

/**
 *
 * Thrown when communitation with Conserv server fails
 * 
 * @author martin.vanek
 *
 */
public class ConservLoadException extends ConservException {

	private static final long serialVersionUID = 1L;

	public ConservLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConservLoadException(String message) {
		super(message);
	}

	public ConservLoadException(Throwable cause) {
		super(cause);
	}

}
