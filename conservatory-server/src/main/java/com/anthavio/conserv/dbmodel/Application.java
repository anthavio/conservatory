package com.anthavio.conserv.dbmodel;

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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "APPLICATION")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "APPLICATION_SEQ", initialValue = 100, allocationSize = 10)
public class Application extends AbstractEntity {

	@Size(min = 3, max = 20)
	@Pattern(regexp = "[a-z0-9]+")
	@Column(name = "CODE_NAME", unique = true, nullable = false)
	private String codeName;

	@OneToMany(mappedBy = "application")
	@OrderBy("CREATED_AT")
	private List<ConfigResource> configResources;

	@Column(name = "ID_GROUP", nullable = true)
	private Long idGroup;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GROUP", referencedColumnName = "ID", insertable = false, updatable = false)
	private LogicalGroup group;

	Application() {
		//JPA
	}

	public Application(String name, String codeName) {
		super(name);
		this.codeName = codeName.toLowerCase();
	}

	public List<ConfigResource> getConfigResources() {
		if (configResources == null) {
			configResources = new ArrayList<ConfigResource>();
		}
		return configResources;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public Long getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Long idGroup) {
		this.idGroup = idGroup;
	}

	public LogicalGroup getGroup() {
		return group;
	}

}
