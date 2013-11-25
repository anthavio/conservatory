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
public interface ConfigTargetDao extends JpaRepository<ConfigTarget, Long>, QueryDslPredicateExecutor<ConfigTarget> {

}
