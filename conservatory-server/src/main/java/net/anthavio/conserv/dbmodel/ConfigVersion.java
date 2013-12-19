package net.anthavio.conserv.dbmodel;

import java.util.List;

import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * 
 * @author martin.vanek
 *
 */
//@Entity
//@Table(name = "CONFIG_VERSION")
public class ConfigVersion extends AbstractEntity {

	@OneToMany(mappedBy = "configVersion")
	@OrderBy("HOSTNAME, CREATED_AT")
	private List<ConfigDownload> downloads;

	@OneToMany(mappedBy = "configVersion")
	private List<ConfigProperty> properties;
}
