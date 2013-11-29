package com.anthavio.conserv.dbmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Secondary table for binary properties
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "BINARY_STORE")
public class BinaryStore {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "JPA_ID_GEN")
	@TableGenerator(name = "JPA_ID_GEN", initialValue = 100, allocationSize = 50, table = "SEQUENCES", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_VALUE")
	@Column(name = "ID")
	private Long id;

	@Lob
	@Column(name = "BINARY_VALUE", columnDefinition = "BLOB NOT NULL")
	private byte[] value;

	BinaryStore() {
		//JPA
	}

	public BinaryStore(byte[] value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public byte[] getValue() {
		return value;
	}

}
