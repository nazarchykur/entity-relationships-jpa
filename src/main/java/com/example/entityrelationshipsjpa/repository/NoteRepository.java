package com.example.entityrelationshipsjpa.repository;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
