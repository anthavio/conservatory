package com.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Mapping table ConfigResource <-> Environment
 * 
 * Also acts as Version for List of ConfigProperties via mapping table CONFIG_VERSION
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_TARGET")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_TARGET_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigTarget extends AbstractEntity {

	@Column(name = "ID_CONFIG_RESOURCE", nullable = false)
	private Long idConfigResource;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_RESOURCE", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigResource configResource;

	@Column(name = "ID_ENVIRONMENT", nullable = false)
	private Long idEnvironment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ENVIRONMENT", referencedColumnName = "ID", insertable = false, updatable = false)
	private Environment environment;

	//@OneToMany(mappedBy = "configTarget")
	//private List<ConfigVersion> versions;

	@OneToMany(mappedBy = "configTarget")
	@OrderBy("HOSTNAME, CREATED_AT")
	private List<ConfigDownload> downloads;

	@ManyToMany
	@JoinTable(name = "TARGET_PROPERTY",// 
	joinColumns = { @JoinColumn(name = "ID_CONFIG_TARGET", referencedColumnName = "ID") },// 
	inverseJoinColumns = { @JoinColumn(name = "ID_CONFIG_PROPERTY", referencedColumnName = "ID") })
	private List<ConfigProperty> properties;

	ConfigTarget() {
		//JPA
	}

	public ConfigTarget(ConfigResource configResource, Environment environment, String name) {
		super(name);
		this.idConfigResource = configResource.getId();
		this.idEnvironment = environment.getId();
	}
}
