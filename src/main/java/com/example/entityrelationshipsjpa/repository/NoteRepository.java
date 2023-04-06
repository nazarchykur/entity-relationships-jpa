package com.example.entityrelationshipsjpa.repository;

import com.example.entityrelationshipsjpa.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
    It is indeed not necessary to put the @Repository annotation on interfaces that extend JpaRepository; 
    Spring recognises the repositories by the fact that they extend one of the predefined Repository interfaces.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findAllByPersonId(Long personId);
}
