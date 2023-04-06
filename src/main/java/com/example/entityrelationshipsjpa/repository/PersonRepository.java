package com.example.entityrelationshipsjpa.repository;

import com.example.entityrelationshipsjpa.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
    It is indeed not necessary to put the @Repository annotation on interfaces that extend JpaRepository; 
    Spring recognises the repositories by the fact that they extend one of the predefined Repository interfaces.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
