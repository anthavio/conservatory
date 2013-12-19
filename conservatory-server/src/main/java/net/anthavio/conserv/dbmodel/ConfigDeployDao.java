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
public interface ConfigDeployDao extends JpaRepository<ConfigDeploy, Long>, QueryDslPredicateExecutor<ConfigDeploy> {

}
