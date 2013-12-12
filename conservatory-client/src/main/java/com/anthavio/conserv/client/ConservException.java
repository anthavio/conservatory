package com.anthavio.conserv.client;

/**
 * 
 * @author martin.vanek
 *
 */
public class ConservException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConservException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConservException(String message) {
		super(message);
	}

	public ConservException(Throwable cause) {
		super(cause);
	}

}
