package com.anthavio.conserv.dbmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
@SequenceGenerator(name = "JPA_SEQ_GEN", sequenceName = "CONFIG_DOWNLOAD_SEQ", allocationSize = 10)
public class ConfigDownload {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JPA_SEQ_GEN")
	private Long id;

	@Column(name = "HOSTNAME", nullable = false, updatable = false)
	private String hostname;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Date created;

	@Column(name = "ID_CONFIG_TARGET", nullable = false, updatable = false)
	private Long idConfigTarget;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CONFIG_TARGET", referencedColumnName = "ID", insertable = false, updatable = false)
	private ConfigTarget configTarget;

	ConfigDownload() {
		//JPA
	}

	public ConfigDownload(ConfigTarget configTarget, String hostname) {
		this.idConfigTarget = configTarget.getId();
		this.hostname = hostname;
		this.created = new Date();
	}
}
