package net.anthavio.conserv.client;

/**
 *
 * Thrown when ConservClient instance initialization fails
 * 
 * @author martin.vanek
 *
 */
public class ConservInitException extends ConservException {

	private static final long serialVersionUID = 1L;

	public ConservInitException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConservInitException(String message) {
		super(message);
	}

	public ConservInitException(Throwable cause) {
		super(cause);
	}

}
