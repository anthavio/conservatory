package com.anthavio.conserv.dbmodel;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * http://docs.spring.io/spring-data/jpa/docs/1.4.2.RELEASE/reference/html/repositories.html#repositories.custom-behaviour-for-all-repositories
 * 
 * @author martin.vanek
 *
 */
public class AbstractDaoFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable> extends
		JpaRepositoryFactoryBean<R, T, I> {

	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

		return new AbstractDaoFactory(entityManager);
	}

	private static class AbstractDaoFactory<T, I extends Serializable> extends JpaRepositoryFactory {

		private EntityManager entityManager;

		public AbstractDaoFactory(EntityManager entityManager) {
			super(entityManager);
			this.entityManager = entityManager;
		}

		protected Object getTargetRepository(RepositoryMetadata metadata) {
			return new AbstractDaoImplx<T, I>((Class<T>) metadata.getDomainType(), entityManager);
		}

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			// The RepositoryMetadata can be safely ignored, it is used by the JpaRepositoryFactory
			//to check for QueryDslJpaRepository's which is out of scope.
			return AbstractDaoItfx.class;
		}
	}

}
