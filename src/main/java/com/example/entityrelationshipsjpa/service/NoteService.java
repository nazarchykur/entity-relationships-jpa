package com.example.entityrelationshipsjpa.service;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import com.example.entityrelationshipsjpa.repository.NoteRepository;
import com.example.entityrelationshipsjpa.repository.PersonRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final PersonRepository personRepository;

    @Transactional
    public void saveNoteWithPerson(Note note, Person person) {
        personRepository.save(person);

        note.setPerson(person);
        noteRepository.save(note);
    }

    @Transactional
    public void saveNewNoteToExistingPerson(Long personId, Note note) {
        // 2. зберегти new Note до існуючого Person (select = 1, insert = 1)
        log.info("2. Save new Note to existing Person (select = 1, insert = 1)");
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalStateException("Person by this id=" + personId + " does not exist"));

        note.setPerson(person);
        noteRepository.save(note);
    }
}
