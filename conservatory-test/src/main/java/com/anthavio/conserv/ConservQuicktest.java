package com.anthavio.conserv;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.anthavio.conserv.dbmodel.Application;
import com.anthavio.conserv.dbmodel.ApplicationDao;
import com.anthavio.conserv.dbmodel.ConfigProperty;
import com.anthavio.conserv.dbmodel.ConfigPropertyDao;
import com.anthavio.conserv.dbmodel.ConfigResource;
import com.anthavio.conserv.dbmodel.ConfigResourceDao;
import com.anthavio.conserv.dbmodel.ConfigTarget;
import com.anthavio.conserv.dbmodel.ConfigTargetDao;
import com.anthavio.conserv.dbmodel.Environment;
import com.anthavio.conserv.dbmodel.EnvironmentDao;
import com.anthavio.conserv.dbmodel.LogicalGroup;
import com.anthavio.conserv.dbmodel.LogicalGroupDao;
import com.anthavio.spring.test.ContextRefLoader;

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
	private ConfigTargetDao targetDao;

	@Autowired
	private ConfigPropertyDao propertyDao;

	@Test
	//@Transactional
	public void test() throws Exception {

		//propertyDao.deleteAllInBatch();
		targetDao.deleteAllInBatch();
		resourceDao.deleteAllInBatch();
		environmentDao.deleteAllInBatch();
		applicationDao.deleteAllInBatch();
		groupDao.deleteAllInBatch();

		LogicalGroup group = new LogicalGroup("Example Group");
		groupDao.save(group);

		Application application = new Application("Example Application", "example");
		application.setIdGroup(group.getId());
		applicationDao.save(application);

		Environment etest = new Environment("Test", "test", true);
		Environment estaging = new Environment("Staging", "staging", true);
		Environment elive = new Environment("Live", "live", true);
		environmentDao.save(etest);
		environmentDao.save(estaging);
		environmentDao.save(elive);

		ConfigResource resource = new ConfigResource("Example Config", "properties", application);
		resourceDao.save(resource);

		ConfigTarget ttest = new ConfigTarget(resource, etest, "Example Config on Test");
		targetDao.save(ttest);
		ConfigTarget tstaging = new ConfigTarget(resource, estaging, "Example Config on Staging");
		targetDao.save(tstaging);
		ConfigTarget tlive = new ConfigTarget(resource, elive, "Example Config on Live");
		targetDao.save(tlive);

		ConfigProperty property1 = new ConfigProperty(tlive, "string.property", "Blah blah blah");
		ConfigProperty property2 = new ConfigProperty(tlive, "integer.property", 123456789);
		ConfigProperty property3 = new ConfigProperty(tlive, "date.property", new Date());
		propertyDao.save(property1);
		propertyDao.save(property2);
		propertyDao.save(property3);

		targetDao.save(tlive); //insert new 

		applicationDao.findAll();
		//applicationDao.findAll();
		//applicationDao.findAll();

		Application application2 = applicationDao.findByName("Example Application");
		System.out.println(application2);
		//applicationDao.findByName("Example Application");
		//applicationDao.findByName("Example Application");
	}
}
