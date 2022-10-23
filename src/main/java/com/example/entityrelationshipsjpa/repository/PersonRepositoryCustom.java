package com.example.entityrelationshipsjpa.repository;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@AllArgsConstructor
@Repository
public class PersonRepositoryCustom {
    private final EntityManager entityManager;

    public void saveNewNoteUsingProxy(Long personId, Note note) {
        Person person = entityManager.getReference(Person.class, personId);
        note.setPerson(person);
        entityManager.persist(note);
    }
}
