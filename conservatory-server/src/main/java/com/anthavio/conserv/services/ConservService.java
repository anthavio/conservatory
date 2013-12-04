package com.anthavio.conserv.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthavio.conserv.dbmodel.Application;
import com.anthavio.conserv.dbmodel.ApplicationDao;
import com.anthavio.conserv.dbmodel.ConfigDeploy;
import com.anthavio.conserv.dbmodel.ConfigDeployDao;
import com.anthavio.conserv.dbmodel.ConfigDocument;
import com.anthavio.conserv.dbmodel.ConfigDocumentDao;
import com.anthavio.conserv.dbmodel.ConfigResource;
import com.anthavio.conserv.dbmodel.ConfigResourceDao;
import com.anthavio.conserv.dbmodel.Environment;
import com.anthavio.conserv.dbmodel.EnvironmentDao;
import com.anthavio.conserv.dbmodel.QApplication;
import com.anthavio.conserv.dbmodel.QConfigDeploy;
import com.anthavio.conserv.dbmodel.QConfigProperty;
import com.anthavio.conserv.dbmodel.QConfigResource;
import com.anthavio.conserv.dbmodel.QEnvironment;
import com.anthavio.util.PropertiesUtil.PropertyLine;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class ConservService {

	@Autowired
	private ApplicationDao applicationDao;

	@Autowired
	private EnvironmentDao environmentDao;

	@Autowired
	private ConfigResourceDao configResourceDao;

	@Autowired
	private ConfigDeployDao configDeployDao;

	@Autowired
	private ConfigDocumentDao documentDao;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private IndexSearchService searchService;

	@Transactional(readOnly = true)
	public List<PropertyLine> loadStepByStep(String envCodeName, String appCodeName, String resourceCodeName) {

		Application application = applicationDao.findByCodeName(appCodeName);
		if (application == null) {
			throw new EmptyResultDataAccessException("Application '" + appCodeName + "' does not exist", 1);
		}

		QConfigResource qcr = QConfigResource.configResource;
		QConfigDeploy qcd = QConfigDeploy.configDeploy;
		QConfigProperty qcp = QConfigProperty.configProperty;

		ConfigResource resource = configResourceDao.findOne(qcr.codeName.eq(resourceCodeName).and(
				qcr.idApplication.eq(application.getId())));
		if (resource == null) {
			throw new EmptyResultDataAccessException("Application '" + appCodeName + "' does not have Resource '"
					+ resourceCodeName + "'", 1);
		}

		Environment environment = environmentDao.findByCodeName(envCodeName);
		if (environment == null) {
			throw new EmptyResultDataAccessException("Environment '" + envCodeName + "' does not exist", 1);
		}

		//FIXME can return more than one result - missing select max(qct.createdAt) subquery
		ConfigDeploy deploy = configDeployDao.findOne(qcd.idConfigResource.eq(resource.getId()).and(
				qcd.idEnvironment.eq(environment.getId())));
		if (deploy == null) {
			throw new EmptyResultDataAccessException("Environment '" + envCodeName + "' does not have Resource '"
					+ resourceCodeName + "'", 1);
		}

		ConfigDocument document = deploy.getDocumentEffective();
		return PropertiesConverter.instance().convert(document.getValue());
	}

	@Transactional(readOnly = true)
	public List<PropertyLine> loadAtOnce(String envCodeName, String appCodeName, String resourceCodeName) {

		QConfigDeploy qDeploy = QConfigDeploy.configDeploy;
		QConfigProperty qProperty = QConfigProperty.configProperty;

		QEnvironment qEnv = QEnvironment.environment;
		BooleanExpression bEnv = qEnv.codeName.eq(envCodeName);

		QApplication qApp = QApplication.application;
		BooleanExpression bApp = qApp.codeName.eq(appCodeName);

		QConfigResource qResource = QConfigResource.configResource;
		BooleanExpression bRes = qResource.codeName.eq(resourceCodeName);

		//BooleanExpression bLatest = qDeploy.createdAt.eq(new JPASubQuery().from(qDeploy).where(qDeploy.state.eq(ConfigDeployState.ACTIVE)).unique(qDeploy.createdAt.max()));
		//configDao.findOne(app.and(env));

		JPAQuery jpq = new JPAQuery(entityManager);

		//combination of leftJoin fetch and distinct is the key here
		//fetch caused that multiple result items are returned
		//distinct prunes duplicities
		List<ConfigDeploy> deploys = jpq.from(qDeploy).innerJoin(qDeploy.configResource(), qResource)
				.innerJoin(qResource.application(), qApp).innerJoin(qDeploy.environment(), qEnv).where(bEnv, bApp, bRes)
				/*.leftJoin(qDeploy.properties, qProperty).fetch().distinct().*/.list(qDeploy);

		//System.out.println(targets);
		if (deploys.size() == 0) {
			throw new EmptyResultDataAccessException("Configuration not found for Environment '" + envCodeName
					+ "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1);
		} else if (deploys.size() == 1) {
			//return targets.get(0).getProperties();
			ConfigDocument document = deploys.get(0).getDocumentEffective();
			return PropertiesConverter.instance().convert(document.getValue());
		} else {
			throw new IncorrectResultSizeDataAccessException("Multiple records of Configuration found for Environment '"
					+ envCodeName + "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1, deploys.size());

		}
	}

	@Transactional
	public void saveDocument(ConfigDocument document) {
		documentDao.save(document);
		searchService.indexDocument(document);
	}

	public List<ConfigDocument> searchDocument(String searchExpression) {
		//We store all ConfigDocument fields in ElasticSearch so far...
		List<ConfigDocument> documents = searchService.searchDocument(searchExpression);
		System.out.println(documents);
		return documents;
	}
}
