package net.anthavio.conserv.dbmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Audit table. Stores request when Configuration is downloaded/requested/fetched by Application (from remote hostname)
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_DOWNLOAD")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_DOWNLOAD_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigDownload {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = AbstractEntity.GENERATOR_NAME)
	@TableGenerator(name = AbstractEntity.GENERATOR_NAME, initialValue = 100, allocationSize = 50, table = AbstractEntity.SEQUENCES_TABLE, pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_VALUE")
	private Long id;

	@Column(name = "HOSTNAME", nullable = false, updatable = false)
	private String hostname;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Date created;

	@Column(name = "ID_CONFIG_DEPLOY", nullable = false, updatable = false)
	private Long idConfigTarget;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_DEPLOY", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigDeploy configDeploy;

	ConfigDownload() {
		//JPA
	}

	public ConfigDownload(ConfigDeploy configTarget, String hostname) {
		this.idConfigTarget = configTarget.getId();
		this.hostname = hostname;
		this.created = new Date();
	}
}
