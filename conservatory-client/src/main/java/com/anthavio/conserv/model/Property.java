package com.anthavio.conserv.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

/**
 * POJO for JAXB or JSON transmission
 * 
 * @author martin.vanek
 *
 */
public class Property implements Serializable {

	public static enum ValueType {

		STRING, INTEGER, PASSWORD, FLOAT, DATETIME, URL, BINARY;
	}

	private static final long serialVersionUID = 1L;

	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private String name;

	private String value;

	private ValueType type;

	private String comment;

	Property() {
		//JAXB
	}

	public Property(String name, String string) {
		this(name, ValueType.STRING, string, null);
	}

	public Property(String name, Integer integer) {
		this(name, ValueType.INTEGER, integer != null ? String.valueOf(integer) : null, null);
	}

	public Property(String name, Date date) {
		this(name, ValueType.DATETIME, date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : null, null);
	}

	public Property(String name, URL url) {
		this(name, ValueType.URL, url != null ? String.valueOf(url) : null, null);
	}

	public Property(String name, URI uri) {
		this(name, ValueType.URL, uri != null ? String.valueOf(uri) : null, null);
	}

	public Property(String name, byte[] binary) {
		this(name, ValueType.BINARY, binary != null ? new String(Base64.encodeBase64(binary, false)) : null, null);
	}

	public Property(String name, ValueType type, String value, String comment) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Property name must be specified");
		}
		this.name = name;
		if (type == null) {
			throw new IllegalArgumentException("Property type must be specified");
		}
		this.type = type;

		//value can be null 
		this.value = value;
		//comment can be null
		this.comment = comment;
	}

	public ValueType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getComment() {
		return comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String asString() {
		return value;
	}

	public Integer asInteger() {
		if (value == null || value.length() == 0) {
			return null;
		} else {
			return Integer.parseInt(value);
		}
	}

	public byte[] asBinary() {
		if (value == null || value.length() == 0) {
			return null;
		} else {
			return Base64.decodeBase64(value.getBytes(Charset.forName("utf-8")));
		}
	}

	public URL asUrl() {
		if (value == null || value.length() == 0) {
			return null;
		} else {
			try {
				return new URL(value);
			} catch (MalformedURLException mux) {
				throw new IllegalStateException("Cannot convert " + value + " to URL", mux);
			}
		}
	}

	public Date asDate() {
		if (value == null || value.length() == 0) {
			return null;
		} else {
			try {
				return new SimpleDateFormat(DATE_FORMAT).parse(value);
			} catch (ParseException px) {
				throw new IllegalStateException("Cannot convert " + value + " to Date", px);
			}
		}
	}

	@Override
	public String toString() {
		return "Property [name=" + name + ", type=" + type + ", value=" + value + ", comment=" + comment + "]";
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Property other = (Property) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
