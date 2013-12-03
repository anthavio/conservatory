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
@Table(name = "BLOB_STORE")
public class BlobStore {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = AbstractEntity.GENERATOR_NAME)
	@TableGenerator(name = AbstractEntity.GENERATOR_NAME, initialValue = 100, allocationSize = 50, table = AbstractEntity.SEQUENCES_TABLE, pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_VALUE")
	@Column(name = "ID")
	private Long id;

	@Lob
	@Column(name = "BLOB_VALUE", columnDefinition = "BLOB NOT NULL")
	private byte[] value;

	BlobStore() {
		//JPA
	}

	public BlobStore(byte[] value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public byte[] getValue() {
		return value;
	}

}
