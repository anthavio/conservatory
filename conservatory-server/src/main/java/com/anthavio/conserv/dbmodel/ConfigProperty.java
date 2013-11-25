package com.anthavio.conserv.dbmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.anthavio.conserv.model.Property.ValueType;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_PROPERTY")
@SequenceGenerator(name = "JPA_SEQ_GEN", sequenceName = "CONFIG_PROPERTY_SEQ", allocationSize = 10)
public class ConfigProperty extends AbstractEntity {

	/*
	@Column(name = "ID_CONFIG_VERSION", nullable = false, updatable = false)
	private Integer idConfigVersion;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_VERSION", insertable = false, updatable = false)
	private ConfigVersion configVersion;
	*/

	@Column(name = "ID_CONFIG_TARGET", nullable = false, updatable = false)
	private Integer idConfigTarget;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_TARGET", insertable = false, updatable = false)
	private ConfigTarget configTarget;

	@Column(name = "TYPE", nullable = false, updatable = false)
	private ValueType type;

	@Column(name = "VALUE", nullable = false, updatable = false)
	private String value;
}
