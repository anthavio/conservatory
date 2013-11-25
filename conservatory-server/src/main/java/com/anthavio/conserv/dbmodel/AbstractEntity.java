package com.anthavio.conserv.dbmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author martin.vanek
 *
 */
@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JPA_SEQ_GEN")
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME", nullable = false, unique = true)
	private String name;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	protected Date createdAt; //set only in constructor

	@Column(name = "COMMENT", nullable = true)
	protected String comment;

	AbstractEntity() {
		//JPA
	}

	/**
	 */
	public AbstractEntity(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Name '" + name + "' must be specified");
		}
		this.name = name;
		this.createdAt = new Date();
	}

	public Long getId() {
		return id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
