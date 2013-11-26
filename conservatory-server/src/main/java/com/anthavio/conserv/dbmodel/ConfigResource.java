package com.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_RESOURCE")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_RESOURCE_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigResource extends AbstractEntity {

	@Column(name = "TYPE", nullable = false)
	private String type; //properties or ....

	@Column(name = "ID_APPLICATION", nullable = false)
	private Long idApplication;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APPLICATION", referencedColumnName = "ID", insertable = false, updatable = false)
	private Application application;

	@OneToMany(mappedBy = "configResource")
	@OrderBy("CREATED_AT")
	private List<ConfigTarget> configTargets;

	public ConfigResource(String name, Application application) {
		super(name);
		this.idApplication = application.getId();
	}
}
