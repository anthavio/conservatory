/**
 * 
 */
package com.anthavio.conserv.dbmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @author martin.vanek
 *
 */
public interface ApplicationDao extends JpaRepository<Application, Long>, QueryDslPredicateExecutor<Application> {

	public Application findByName(String name);

	public Application findByCodeName(String name);
}
