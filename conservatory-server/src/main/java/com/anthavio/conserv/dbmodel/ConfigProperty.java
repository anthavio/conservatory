package com.anthavio.conserv.dbmodel;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Base64;

import com.anthavio.conserv.model.Property;
import com.anthavio.conserv.model.Property.ValueType;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_PROPERTY")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_PROPERTY_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigProperty extends AbstractEntity {

	/*
	@Column(name = "ID_CONFIG_VERSION", nullable = false, updatable = false)
	private Integer idConfigVersion;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_VERSION", insertable = false, updatable = false)
	private ConfigVersion configVersion;
	*/
	/*
	@NotNull
	@Column(name = "ID_CONFIG_TARGET", nullable = false, updatable = false)
	private Long idConfigTarget;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_TARGET", insertable = false, updatable = false)
	private ConfigTarget configTarget;
	*/
	@NotNull
	@Column(name = "PROP_TYPE", nullable = false, updatable = false)
	private ValueType type;

	@Column(name = "PROP_VALUE", nullable = false, updatable = false)
	private String value;

	ConfigProperty() {
		//JPA
	}

	public ConfigProperty(ConfigTarget configTarget, String name, String string) {
		this(configTarget, name, ValueType.STRING, string, null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, Integer integer) {
		this(configTarget, name, ValueType.INTEGER, integer != null ? String.valueOf(integer) : null, null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, Date date) {
		this(configTarget, name, ValueType.DATETIME, date != null ? new SimpleDateFormat(Property.DATE_FORMAT).format(date)
				: null, null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, URL url) {
		this(configTarget, name, ValueType.URL, url != null ? String.valueOf(url) : null, null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, URI uri) {
		this(configTarget, name, ValueType.URL, uri != null ? String.valueOf(uri) : null, null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, byte[] binary) {
		this(configTarget, name, ValueType.BINARY, binary != null ? new String(Base64.encodeBase64(binary, false)) : null,
				null);
	}

	public ConfigProperty(ConfigTarget configTarget, String name, ValueType type, String value, String comment) {
		super(name);
		//this.idConfigTarget = configTarget.getId();
		//this.configTarget = configTarget;
		//this.configTarget.getProperties().add(this);
		configTarget.getProperties().add(this);
		if (type == null) {
			throw new IllegalArgumentException("Config value type must not be null");
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

	public void setType(ValueType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	/*
		public Long getIdConfigTarget() {
			return idConfigTarget;
		}

		public ConfigTarget getConfigTarget() {
			return configTarget;
		}
	*/

}
