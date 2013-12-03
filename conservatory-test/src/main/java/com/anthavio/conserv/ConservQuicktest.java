package com.anthavio.conserv;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.anthavio.conserv.dbmodel.Application;
import com.anthavio.conserv.dbmodel.ApplicationDao;
import com.anthavio.conserv.dbmodel.ConfigDocumentDao;
import com.anthavio.conserv.dbmodel.ConfigDeploy;
import com.anthavio.conserv.dbmodel.ConfigDeployDao;
import com.anthavio.conserv.dbmodel.ConfigDocument;
import com.anthavio.conserv.dbmodel.ConfigResource;
import com.anthavio.conserv.dbmodel.ConfigResourceDao;
import com.anthavio.conserv.dbmodel.Environment;
import com.anthavio.conserv.dbmodel.EnvironmentDao;
import com.anthavio.conserv.dbmodel.LogicalGroup;
import com.anthavio.conserv.dbmodel.LogicalGroupDao;
import com.anthavio.conserv.model.Property;
import com.anthavio.conserv.services.PropertiesConverter;
import com.anthavio.spring.test.ContextRefLoader;
import com.anthavio.util.PropertiesUtil.PropertyLine;

/**
 * 
 * @author martin.vanek
 *
 */
@ContextConfiguration(locations = { "conserv-services", "classpath:spring-locator.xml" }, loader = ContextRefLoader.class)
public class ConservQuicktest extends AbstractTestNGSpringContextTests {

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

	@Test
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
	public void exampleData() throws Exception {

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
		documentDao.save(documentTest);

		ConfigDocument documentStaging = new ConfigDocument(deployStaging, propString);
		documentDao.save(documentStaging);

		ConfigDocument documentLive = new ConfigDocument(deployLive, propString);
		documentDao.save(documentLive);

		//Set effective version of ConfigDocument into ConfigDeploy
		deployTest.setIdDocument(documentTest.getId());
		deployDao.save(deployTest);
		deployStaging.setIdDocument(documentStaging.getId());
		deployDao.save(deployStaging);
		deployLive.setIdDocument(documentLive.getId());
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
