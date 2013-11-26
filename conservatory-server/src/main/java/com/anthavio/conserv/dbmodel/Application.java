package com.anthavio.conserv.dbmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * 
 * @author martin.vanek
 *
 */
@Entity
@Table(name = "APPLICATION")
//@SequenceGenerator(name = "JPA_ID_GEN", sequenceName = "APPLICATION_SEQ", initialValue = 100, allocationSize = 10)
public class Application extends AbstractEntity {

	@OneToMany(mappedBy = "application")
	@OrderBy("CREATED_AT")
	private List<ConfigResource> configResources;

	Application() {
		//JPA
	}

	public Application(String name) {
		super(name);
	}

	public List<ConfigResource> getConfigResources() {
		if (configResources == null) {
			configResources = new ArrayList<ConfigResource>();
		}
		return configResources;
	}

}
