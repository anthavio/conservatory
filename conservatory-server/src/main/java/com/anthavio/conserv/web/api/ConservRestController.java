package com.anthavio.conserv.web.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anthavio.conserv.dbmodel.ApplicationDao;
import com.anthavio.conserv.dbmodel.ConfigProperty;
import com.anthavio.conserv.dbmodel.ConfigTargetDao;
import com.anthavio.conserv.dbmodel.QConfigProperty;
import com.anthavio.conserv.dbmodel.QConfigTarget;
import com.anthavio.conserv.model.Configuration;
import com.anthavio.conserv.model.Property;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

/**
 * 
 * @author martin.vanek
 *
 */
@Controller
public class ConservRestController {

	@Autowired
	private ApplicationDao applicationDao;

	@Autowired
	private ConfigTargetDao configDao;

	@PersistenceContext
	private EntityManager em;

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody
	Configuration getConfigurationJson(@PathVariable String application, @PathVariable String environment) {
		//JPQLQuery query = from(session, productionPlan);
		QConfigTarget ct = QConfigTarget.configTarget;
		BooleanExpression app = ct.configResource().application().name.eq(application);
		BooleanExpression env = ct.environment().name.eq(environment);

		configDao.findOne(app.and(env));

		QConfigProperty cp = QConfigProperty.configProperty;
		JPAQuery jpq = new JPAQuery(em);
		List<ConfigProperty> propertiesx = jpq.from(ct).where(app.and(env)).singleResult(ct.properties);

		ArrayList<Property> properties = new ArrayList<Property>();
		properties.add(new Property("binary.property", new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7,
				8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4,
				5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
		properties.add(new Property("string.property", "Some value"));
		properties.add(new Property("integer.property", 666));
		properties.add(new Property("url.property", URI.create("http://test-www.nature.com:8080/zxzxzx")));
		return new Configuration(application, environment, properties);
	}

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET, headers = "Accept=application/xml")
	public @ResponseBody
	JAXBElement<Configuration> getConfigurationXml(@PathVariable String application, @PathVariable String environment) {
		Configuration configuration = getConfigurationJson(application, environment);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}

	@RequestMapping(value = "config/{application}/{environment}", method = RequestMethod.GET)
	public @ResponseBody
	JAXBElement<Configuration> getConfiguration(@PathVariable String application, @PathVariable String environment) {
		Configuration configuration = getConfigurationJson(application, environment);
		return new JAXBElement<Configuration>(new QName("configuration"), Configuration.class, configuration);
	}
}
