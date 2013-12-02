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
import com.anthavio.conserv.dbmodel.ConfigProperty;
import com.anthavio.conserv.dbmodel.ConfigPropertyDao;
import com.anthavio.conserv.dbmodel.ConfigResource;
import com.anthavio.conserv.dbmodel.ConfigResourceDao;
import com.anthavio.conserv.dbmodel.ConfigTarget;
import com.anthavio.conserv.dbmodel.ConfigTargetDao;
import com.anthavio.conserv.dbmodel.Environment;
import com.anthavio.conserv.dbmodel.EnvironmentDao;
import com.anthavio.conserv.dbmodel.QApplication;
import com.anthavio.conserv.dbmodel.QConfigProperty;
import com.anthavio.conserv.dbmodel.QConfigResource;
import com.anthavio.conserv.dbmodel.QConfigTarget;
import com.anthavio.conserv.dbmodel.QEnvironment;
import com.mysema.query.jpa.JPASubQuery;
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
	private ConfigTargetDao configTargetDao;

	@Autowired
	private ConfigPropertyDao configPropertyDao;

	@PersistenceContext
	private EntityManager em;

	@Transactional(readOnly = true)
	public List<ConfigProperty> loadDebug(String envCodeName, String appCodeName, String resourceCodeName) {

		Application application = applicationDao.findByCodeName(appCodeName);
		if (application == null) {
			throw new EmptyResultDataAccessException("Application '" + appCodeName + "' does not exist", 1);
		}

		QConfigResource qcr = QConfigResource.configResource;
		QConfigTarget qct = QConfigTarget.configTarget;
		QConfigProperty qcp = QConfigProperty.configProperty;

		ConfigResource resource = configResourceDao.findOne(qcr.codeName.eq(resourceCodeName).and(
				qcr.idApplication.eq(application.getId())));
		if (resource == null) {
			throw new EmptyResultDataAccessException("Application '" + appCodeName + "' does not have ConfigResource '"
					+ resourceCodeName + "'", 1);
		}

		Environment environment = environmentDao.findByCodeName(envCodeName);
		if (environment == null) {
			throw new EmptyResultDataAccessException("Environment '" + envCodeName + "' does not exist", 1);
		}

		//FIXME can return more than one result - missing select max(qct.createdAt) subquery
		ConfigTarget target = configTargetDao.findOne(qct.idConfigResource.eq(resource.getId()).and(
				qct.idEnvironment.eq(environment.getId())));
		if (target == null) {
			throw new EmptyResultDataAccessException("Environment '" + envCodeName + "' does not have ConfigResource '"
					+ resourceCodeName + "'", 1);
		}

		target.getProperties().size();//Lazy Load

		return target.getProperties();
	}

	@Transactional(readOnly = true)
	public List<ConfigProperty> loadFast(String envCodeName, String appCodeName, String resourceCodeName) {

		QConfigTarget qTarget = QConfigTarget.configTarget;
		QConfigProperty qProperty = QConfigProperty.configProperty;

		QEnvironment qEnv = QEnvironment.environment;
		BooleanExpression bEnv = qEnv.codeName.eq(envCodeName);

		QApplication qApp = QApplication.application;
		BooleanExpression bApp = qApp.codeName.eq(appCodeName);

		QConfigResource qResource = QConfigResource.configResource;
		BooleanExpression bRes = qResource.codeName.eq(resourceCodeName);

		BooleanExpression bLatest = qTarget.createdAt.eq(new JPASubQuery().from(qTarget).unique(qTarget.createdAt.max()));
		//configDao.findOne(app.and(env));

		JPAQuery jpq = new JPAQuery(em);

		//combination of leftJoin fetch and distinct is the key here
		//fetch caused that multiple result items are returned
		//distinct prunes duplicities
		List<ConfigTarget> targets = jpq.from(qTarget).innerJoin(qTarget.configResource(), qResource)
				.innerJoin(qResource.application(), qApp).innerJoin(qTarget.environment(), qEnv)
				.where(bEnv, bApp, bRes, bLatest).leftJoin(qTarget.properties, qProperty).fetch().distinct().list(qTarget);

		//System.out.println(targets);
		if (targets.size() == 0) {
			throw new EmptyResultDataAccessException("Configuration not found for Environment '" + envCodeName
					+ "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1);
		} else if (targets.size() == 1) {
			return targets.get(0).getProperties();
		} else {
			throw new IncorrectResultSizeDataAccessException("Multiple records of Configuration found for Environment '"
					+ envCodeName + "' Application '" + appCodeName + "' Resource '" + resourceCodeName + "'", 1, targets.size());

		}
	}
}
