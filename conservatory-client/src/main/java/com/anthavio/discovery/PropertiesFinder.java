package com.anthavio.discovery;

import com.anthavio.discovery.PropertiesDiscovery.Result;

/**
 * 
 * @author martin.vanek
 *
 */
public interface PropertiesFinder<T> {

	public Result<T> find();
}
