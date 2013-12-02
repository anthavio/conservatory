package com.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "LOGICAL_GROUP")
public class LogicalGroup extends AbstractEntity {

	@Column(name = "ID_PARENT", nullable = true)
	private Long idParent;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PARENT", referencedColumnName = "ID", insertable = false, updatable = false)
	private LogicalGroup parent;

	@OneToMany(mappedBy = "parent")
	private List<LogicalGroup> children;

	@OneToMany(mappedBy = "group")
	private List<Application> applications;

	LogicalGroup() {
		//JPA
	}

	public LogicalGroup(String name) {
		super(name);
	}

	public Long getIdParent() {
		return idParent;
	}

	public void setIdParent(Long idParent) {
		this.idParent = idParent;
	}

	public LogicalGroup getParent() {
		return parent;
	}

	public List<LogicalGroup> getChildren() {
		return children;
	}

}
