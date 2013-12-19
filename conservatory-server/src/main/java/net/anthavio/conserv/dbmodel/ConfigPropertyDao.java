/**
 * 
 */
package net.anthavio.conserv.dbmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @author martin.vanek
 *
 */
public interface ConfigPropertyDao extends JpaRepository<ConfigProperty, Long>,
		QueryDslPredicateExecutor<ConfigProperty> {

	public Application findByName(String name);
}
