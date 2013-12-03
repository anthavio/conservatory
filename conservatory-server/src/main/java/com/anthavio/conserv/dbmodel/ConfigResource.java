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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "CONFIG_RESOURCE")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "CONFIG_RESOURCE_SEQ", initialValue = 100, allocationSize = 10)
public class ConfigResource extends AbstractEntity {

	public static enum ConfigResourceType {
		PROPERTIES;
	}

	@Size(min = 3, max = 20)
	@Pattern(regexp = "[a-z0-9]+")
	@Column(name = "CODE_NAME", unique = false, nullable = false)
	private String codeName;

	@NotNull
	@Column(name = "TYPE", nullable = false)
	private ConfigResourceType type; //properties or ....

	@NotNull
	@Column(name = "ID_APPLICATION", nullable = false)
	private Long idApplication;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APPLICATION", referencedColumnName = "ID", insertable = false, updatable = false)
	private Application application;

	@OneToMany(mappedBy = "configResource")
	@OrderBy("CREATED_AT")
	private List<ConfigDeploy> configDeploys;

	ConfigResource() {
		//JPA
	}

	public ConfigResource(String name, String codeName, Application application) {
		super(name);
		this.codeName = codeName;
		this.type = ConfigResourceType.PROPERTIES;
		this.idApplication = application.getId();
	}
}
