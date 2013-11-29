package com.anthavio.conserv.dbmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	@Column(name = "SHORT_NAME", nullable = true)
	private String shortName;

	@OneToMany(mappedBy = "application")
	@OrderBy("CREATED_AT")
	private List<ConfigResource> configResources;

	Application() {
		//JPA
	}

	public Application(String name, String shortName) {
		super(name);
		if (isAlphanumeric(shortName)) {
			this.shortName = shortName.toLowerCase();
		} else {
			throw new IllegalArgumentException("Short name must be alphanumeric +'" + shortName + "'");
		}
	}

	public List<ConfigResource> getConfigResources() {
		if (configResources == null) {
			configResources = new ArrayList<ConfigResource>();
		}
		return configResources;
	}

}
