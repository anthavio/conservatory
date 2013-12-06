package com.anthavio.conserv.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * POJO for JAXB or JSON transmission
 * 
 * @author martin.vanek
 *
 */
public class Config implements Serializable {

	private static final long serialVersionUID = 1L;

	private String aplication;

	private String environment;

	private Date createdAt;

	private List<Property> properties;

	Config() {
		//JAXB
	}

	public Config(String aplication, String environment, Date createdAt, List<Property> properties) {
		this.aplication = aplication;
		this.environment = environment;
		this.createdAt = createdAt;
		this.properties = properties;
	}

	public Property getProperty(String name) {
		for (Property property : properties) {
			if (property.getName().equals(name)) {
				return property;
			}
		}
		return null;
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Configuration [aplication=" + aplication + ", environment=" + environment + ", createdAt=" + createdAt
				+ ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aplication == null) ? 0 : aplication.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
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
		Config other = (Config) obj;
		if (aplication == null) {
			if (other.aplication != null)
				return false;
		} else if (!aplication.equals(other.aplication))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
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
