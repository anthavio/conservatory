package net.anthavio.conserv.dbmodel;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * http://docs.spring.io/spring-data/jpa/docs/1.4.2.RELEASE/reference/html/repositories.html#repositories.custom-behaviour-for-all-repositories
 * 
 * @author martin.vanek
 *
 */
public class AbstractDaoImplx<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements
		AbstractDaoItfx<T, ID> {

	private EntityManager entityManager;

	public AbstractDaoImplx(Class<T> domainClass, EntityManager entityManager) {
		super(domainClass, entityManager);
		this.entityManager = entityManager;
	}

	public void sharedCustomMethod(ID id) {
		System.out.println("prdel!");
	}

}
