package com.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Strong Entity
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "ENVIRONMENT")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "ENVIRONMENT_SEQ", initialValue = 100, allocationSize = 10)
public class Environment extends AbstractEntity {

	@Size(min = 3, max = 20)
	@Column(name = "SHORT_NAME", nullable = true)
	private String shortName;

	@Column(name = "STATUS", nullable = false)
	private Integer status; //global, private, deleted

	@OneToMany(mappedBy = "environment")
	@OrderBy("CREATED_AT")
	private List<ConfigTarget> configTargets;

	Environment() {
		//JPA
	}

	public Environment(String name, String shortName, boolean global) {
		super(name);
		this.status = global ? 1 : 2;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
