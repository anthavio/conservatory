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
public interface ConfigResourceDao extends JpaRepository<ConfigResource, Long>,
		QueryDslPredicateExecutor<ConfigResource> {

	public Application findByName(String name);
}
