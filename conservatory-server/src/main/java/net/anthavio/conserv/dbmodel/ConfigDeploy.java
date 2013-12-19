package net.anthavio.conserv.dbmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Mapping table ConfigResource <-> Environment and ConfigDocument versions
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_DEPLOY")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_TARGET_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigDeploy extends AbstractEntity {

	@NotNull
	@Column(name = "ID_CONFIG_RESOURCE", nullable = false)
	private Long idConfigResource;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_RESOURCE", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigResource configResource;

	@NotNull
	@Column(name = "ID_ENVIRONMENT", nullable = false)
	private Long idEnvironment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ENVIRONMENT", referencedColumnName = "ID", insertable = false, updatable = false)
	private Environment environment;

	@Column(name = "ID_CONFIG_DOCUMENT", nullable = true, updatable = true)
	private Long idDocumentEffective; //effective version of the document

	//OneToOne creates UNIQUE constraint on this column
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_CONFIG_DOCUMENT", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigDocument documentEffective; //effective version of the document

	@OneToMany(mappedBy = "configDeploy")
	@OrderBy("CREATED_AT")
	private List<ConfigDocument> documentVersions;

	@OneToMany(mappedBy = "configDeploy")
	@OrderBy("HOSTNAME, CREATED_AT")
	private List<ConfigDownload> downloads;

	/*
		@ManyToMany(cascade = { CascadeType.ALL })
		@JoinTable(name = "TARGET_PROPERTY",// 
		joinColumns = { @JoinColumn(name = "ID_CONFIG_TARGET", referencedColumnName = "ID") },// 
		inverseJoinColumns = { @JoinColumn(name = "ID_CONFIG_PROPERTY", referencedColumnName = "ID") })
		private List<ConfigProperty> properties;
	*/
	ConfigDeploy() {
		//JPA
	}

	public ConfigDeploy(ConfigResource configResource, Environment environment, String name) {
		super(name);
		this.idConfigResource = configResource.getId();
		this.configResource = configResource;
		this.idEnvironment = environment.getId();
		this.environment = environment;
	}

	public void setIdDocumentEffective(Long idDocumentEffective) {
		this.idDocumentEffective = idDocumentEffective;
	}

	public Long getIdDocumentEffective() {
		return idDocumentEffective;
	}

	public ConfigDocument getDocumentEffective() {
		return documentEffective;
	}

	public Long getIdEnvironment() {
		return idEnvironment;
	}

	public void setIdEnvironment(Long idEnvironment) {
		this.idEnvironment = idEnvironment;
	}

	public Long getIdConfigResource() {
		return idConfigResource;
	}

	public ConfigResource getConfigResource() {
		return configResource;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public List<ConfigDownload> getDownloads() {
		if (downloads == null) {
			downloads = new ArrayList<ConfigDownload>();
		}
		return downloads;
	}
	/*
		public List<ConfigProperty> getProperties() {
			if (properties == null) {
				properties = new ArrayList<ConfigProperty>();
			}
			return properties;
		}
	*/
}
