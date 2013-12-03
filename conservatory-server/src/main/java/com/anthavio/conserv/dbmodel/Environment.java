package com.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
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

	public static enum EnvironmentState {
		PUBLIC, PRIVATE, DELETED;
	}

	@Size(min = 3, max = 20)
	@Pattern(regexp = "[a-z0-9]+")
	@Column(name = "CODE_NAME", unique = true, nullable = false)
	private String codeName;

	@Column(name = "STATUS", nullable = false)
	private EnvironmentState state; //global, private, deleted

	@OneToMany(mappedBy = "environment")
	@OrderBy("CREATED_AT")
	private List<ConfigDeploy> configDeploys;

	Environment() {
		//JPA
	}

	public Environment(String name, String codeName, boolean global) {
		super(name);
		this.state = global ? EnvironmentState.PUBLIC : EnvironmentState.PRIVATE;
		this.codeName = codeName.toLowerCase();
	}

	public EnvironmentState getState() {
		return state;
	}

	public void setStatus(EnvironmentState state) {
		this.state = state;
	}

}
