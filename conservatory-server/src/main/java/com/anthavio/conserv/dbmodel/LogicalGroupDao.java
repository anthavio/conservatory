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
public interface LogicalGroupDao extends JpaRepository<LogicalGroup, Long>, QueryDslPredicateExecutor<LogicalGroup> {

	public Application findByName(String name);

}
