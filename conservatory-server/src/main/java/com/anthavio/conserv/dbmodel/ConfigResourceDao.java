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

	public ConfigResource findByName(String name);

	public ConfigResource findByCodeName(String name);
}
