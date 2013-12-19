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
public interface EnvironmentDao extends JpaRepository<Environment, Long>, QueryDslPredicateExecutor<Environment> {

	public Environment findByName(String name);

	public Environment findByCodeName(String name);

}
