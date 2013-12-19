package net.anthavio.discovery;

import net.anthavio.discovery.PropertiesDiscovery.Result;

/**
 * 
 * @author martin.vanek
 *
 */
public interface PropertiesFinder<T> {

	public Result<T> find();
}
