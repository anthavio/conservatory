package net.anthavio.conserv.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import net.anthavio.conserv.dbmodel.QApplication;
import net.anthavio.conserv.dbmodel.QConfigDeploy;
import net.anthavio.conserv.dbmodel.QConfigProperty;
import net.anthavio.conserv.dbmodel.QConfigResource;
import net.anthavio.conserv.dbmodel.QEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public void saveDocument(ConfigDocument document) {
		documentDao.save(document);
		searchService.indexDocument(document);
	}

	@Transactional(readOnly = true)
	public ConfigDocument loadStepByStep(String envCodeName, String appCodeName, String resourceCodeName) {

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
		document.getValue(); //Lazy load
		return document;
		//return PropertiesConverter.instance().convert(document.getValue());
	}

	@Transactional(readOnly = true)
	public ConfigDocument loadAtOnce(String envCodeName, String appCodeName, String resourceCodeName) {

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
		List<ConfigDeploy> deploys = jpq.from(qDeploy).innerJoin(qDeploy.configResource(), qResource).fetch()
				.innerJoin(qResource.application(), qApp).fetch().innerJoin(qDeploy.environment(), qEnv).fetch()
				.where(bEnv, bApp, bRes)
				/*.leftJoin(qDeploy.properties, qProperty).fetch().distinct().*/.list(qDeploy);

		if (deploys.size() == 0) {
			throw new EmptyResultDataAccessException("Configuration not found for Environment '" + envCodeName
					+ "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1);
		} else if (deploys.size() == 1) {
			//return targets.get(0).getProperties();
			ConfigDocument document = deploys.get(0).getDocumentEffective();
			document.getValue(); //Lazy load
			return document;
		} else {
			throw new IncorrectResultSizeDataAccessException("Multiple records of Configuration found for Environment '"
					+ envCodeName + "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1, deploys.size());

		}
	}

	public List<ConfigDocument> searchDocument(String searchExpression) {
		//We store all ConfigDocument fields in ElasticSearch so far...
		List<ConfigDocument> documents = searchService.searchDocument(searchExpression);
		System.out.println(documents);
		return documents;
	}
}
