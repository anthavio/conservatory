package com.anthavio.conserv.dbmodel;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Cannot extend QueryDslPredicateExecutor interface -> No property find found for type ....
 * 
 * @author martin.vanek
 *
 */
public interface AbstractDaoItfx<T, ID extends Serializable> extends JpaRepository<T, ID>/*, QueryDslPredicateExecutor<T>*/{

	//void sharedCustomMethod(ID id);
}
