package com.example.entityrelationshipsjpa.repository;

import com.example.entityrelationshipsjpa.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
