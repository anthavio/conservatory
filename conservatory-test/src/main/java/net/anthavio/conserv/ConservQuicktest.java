package net.anthavio.conserv;

import java.util.List;

import net.anthavio.conserv.dbmodel.Application;
import net.anthavio.conserv.dbmodel.ApplicationDao;
import net.anthavio.conserv.dbmodel.ConfigDeploy;
import net.anthavio.conserv.dbmodel.ConfigDeployDao;
import net.anthavio.conserv.dbmodel.ConfigDocument;
import net.anthavio.conserv.dbmodel.ConfigDocumentDao;
import net.anthavio.conserv.dbmodel.ConfigResource;
import net.anthavio.conserv.dbmodel.ConfigResourceDao;
import net.anthavio.conserv.dbmodel.Environment;
import net.anthavio.conserv.dbmodel.EnvironmentDao;
import net.anthavio.conserv.dbmodel.LogicalGroup;
import net.anthavio.conserv.dbmodel.LogicalGroupDao;
import net.anthavio.conserv.model.Property;
import net.anthavio.conserv.services.ConservService;
import net.anthavio.conserv.services.PropertiesConverter;
import net.anthavio.spring.test.ContextRefLoader;
import net.anthavio.util.PropertiesUtil.PropertyLine;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;


/**
 * 
 * @author martin.vanek
 *
 */
@ContextConfiguration(locations = { "conserv-services", "classpath:spring-locator.xml" }, loader = ContextRefLoader.class)
public class ConservQuicktest extends AbstractTransactionalTestNGSpringContextTests {

	@Autowired
	private LogicalGroupDao groupDao;

	@Autowired
	private ApplicationDao applicationDao;

	@Autowired
	private EnvironmentDao environmentDao;

	@Autowired
	private ConfigResourceDao resourceDao;

	@Autowired
	private ConfigDeployDao deployDao;

	@Autowired
	private ConfigDocumentDao documentDao;

	@Autowired
	private ConservService service;

	//@Test
	public void properties() throws Exception {
		String propString = IOUtils.toString(getClass().getResourceAsStream("/example.properties"), "UTF-8");
		List<PropertyLine> plines = PropertiesConverter.instance().convert(propString);
		for (PropertyLine propertyLine : plines) {
			//System.out.println(propertyLine.getName());
			System.out.println(propertyLine.toFileLine());
		}

		List<Property> clines = PropertiesConverter.instance().convertForClient(plines);
		for (Property property : clines) {
			System.out.println(property);
		}

	}

	@Test
	@Rollback(false)
	public void exampleData() throws Exception {
		List<ConfigDeploy> deploys = deployDao.findAll();
		for (ConfigDeploy deploy : deploys) {
			deploy.setIdDocumentEffective(null);
			deployDao.saveAndFlush(deploy);
		}
		documentDao.deleteAllInBatch();
		deployDao.deleteAllInBatch();
		documentDao.deleteAllInBatch();
		resourceDao.deleteAllInBatch();
		environmentDao.deleteAllInBatch();
		applicationDao.deleteAllInBatch();
		groupDao.deleteAllInBatch();

		LogicalGroup group = new LogicalGroup("Example Group");
		groupDao.save(group);

		Application application = new Application("Example Application", "example");
		application.setIdGroup(group.getId());
		applicationDao.save(application);

		Environment envTest = new Environment("Test", "test", true);
		Environment envStaging = new Environment("Staging", "staging", true);
		Environment envLive = new Environment("Live", "live", true);
		environmentDao.save(envTest);
		environmentDao.save(envStaging);
		environmentDao.save(envLive);

		ConfigResource resource = new ConfigResource("Example Config", "properties", application);
		resourceDao.save(resource);

		//There is circular dependence between ConfigDocument and ConfigDeploy

		String propString = IOUtils.toString(getClass().getResourceAsStream("/example.properties"), "UTF-8");

		ConfigDeploy deployTest = new ConfigDeploy(resource, envTest, "Example Config on Test");
		deployDao.save(deployTest);
		ConfigDeploy deployStaging = new ConfigDeploy(resource, envStaging, "Example Config on Staging");
		deployDao.save(deployStaging);
		ConfigDeploy deployLive = new ConfigDeploy(resource, envLive, "Example Config on Live");
		deployDao.save(deployLive);

		ConfigDocument documentTest = new ConfigDocument(deployTest, propString);
		service.saveDocument(documentTest);

		ConfigDocument documentStaging = new ConfigDocument(deployStaging, propString);
		service.saveDocument(documentStaging);

		ConfigDocument documentLive = new ConfigDocument(deployLive, propString);
		service.saveDocument(documentLive);

		//Set effective version of ConfigDocument into ConfigDeploy
		deployTest.setIdDocumentEffective(documentTest.getId());
		deployDao.save(deployTest);
		deployStaging.setIdDocumentEffective(documentStaging.getId());
		deployDao.save(deployStaging);
		deployLive.setIdDocumentEffective(documentLive.getId());
		deployDao.save(deployLive);

		/*
		ConfigProperty property1 = new ConfigProperty(tlive, "string.property", "Blah blah blah");
		ConfigProperty property2 = new ConfigProperty(tlive, "integer.property", 123456789);
		ConfigProperty property3 = new ConfigProperty(tlive, "date.property", new Date());
		propertyDao.save(property1);
		propertyDao.save(property2);
		propertyDao.save(property3);
		 */
		//deployDao.save(tlive); //insert new 

		applicationDao.findAll();
		//applicationDao.findAll();
		//applicationDao.findAll();

		Application application2 = applicationDao.findByName("Example Application");
		System.out.println(application2);
		//applicationDao.findByName("Example Application");
		//applicationDao.findByName("Example Application");
	}
}
