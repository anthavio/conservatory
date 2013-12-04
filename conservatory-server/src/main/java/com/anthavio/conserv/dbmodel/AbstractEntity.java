package com.anthavio.conserv.dbmodel;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

/**
 * @author martin.vanek
 *
 */
@MappedSuperclass
@Cacheable
public abstract class AbstractEntity {

	public static final String GENERATOR_NAME = "JPA_ID_GEN";

	public static final String SEQUENCES_TABLE = "ID_SEQUENCES";

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = GENERATOR_NAME)
	@TableGenerator(name = AbstractEntity.GENERATOR_NAME, initialValue = 100, allocationSize = 50, table = SEQUENCES_TABLE, pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_VALUE")
	@Column(name = "ID")
	private Long id;

	@Size(min = 3, max = 60)
	@Column(name = "NAME", nullable = false)
	private String name;

	@Temporal(TemporalType.TIMESTAMP)
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

	public static boolean isAlphanumeric(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}
		for (int i = 0; i < string.length(); ++i) {
			if (!Character.isLetterOrDigit(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", createdAt=" + createdAt + "]";
	}

}
