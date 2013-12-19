package net.anthavio.conserv.dbmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * CLOB store for Configuration File
 * 
 * More versions of ConfigDocument exist for single ConfigDeploy, but only one is "effective"  
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_DOCUMENT")
public class ConfigDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = AbstractEntity.GENERATOR_NAME)
	@TableGenerator(name = AbstractEntity.GENERATOR_NAME, initialValue = 100, allocationSize = 50, table = AbstractEntity.SEQUENCES_TABLE, pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_VALUE")
	@Column(name = "ID")
	private Long id;

	@Lob
	@Column(name = "CLOB_VALUE", columnDefinition = "CLOB NOT NULL", updatable = false)
	private String value;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Date createdAt; //set only in constructor

	@NotNull
	@Column(name = "ID_CONFIG_DEPLOY", nullable = false, updatable = false)
	private Long idConfigDeploy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_DEPLOY", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigDeploy configDeploy;

	@PreUpdate
	public void preUpdate() {
		throw new IllegalStateException("ConfigDocument is insertable only and must not be updated");
	}

	ConfigDocument() {
		//JPA
	}

	/**
	 * If loaded from search index store...
	 */
	public ConfigDocument(Long id, Long idConfigDeploy, Date createdAt, String value) {
		this.id = id;
		this.idConfigDeploy = idConfigDeploy;
		this.createdAt = createdAt;
		this.value = value;
	}

	public ConfigDocument(ConfigDeploy configDeploy, String value) {
		this.idConfigDeploy = configDeploy.getId();
		this.configDeploy = configDeploy;
		this.value = value;
		this.createdAt = new Date();
	}

	public Long getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Long getIdConfigDeploy() {
		return idConfigDeploy;
	}

	public ConfigDeploy getConfigDeploy() {
		return configDeploy;
	}

	@Override
	public String toString() {
		return "ConfigDocument [id=" + id + ", idConfigDeploy=" + idConfigDeploy + ", createdAt=" + createdAt + "]";
	}

}
