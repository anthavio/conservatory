package com.anthavio.conserv.model;

import java.io.Serializable;
import java.util.List;

/**
 * POJO for JAXB or JSON transmission
 * 
 * @author martin.vanek
 *
 */
public class Configuration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String aplication;

	private String environment;

	private List<Property> properties;

	Configuration() {
		//JAXB
	}

	public Configuration(String aplication, String environment, List<Property> properties) {
		this.aplication = aplication;
		this.environment = environment;
		this.properties = properties;
	}

	public String getAplication() {
		return aplication;
	}

	public String getEnvironment() {
		return environment;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setAplication(String aplication) {
		this.aplication = aplication;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Configuration [aplication=" + aplication + ", environment=" + environment + ", properties=" + properties
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aplication == null) ? 0 : aplication.hashCode());
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuration other = (Configuration) obj;
		if (aplication == null) {
			if (other.aplication != null)
				return false;
		} else if (!aplication.equals(other.aplication))
			return false;
		if (environment == null) {
			if (other.environment != null)
				return false;
		} else if (!environment.equals(other.environment))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

}
