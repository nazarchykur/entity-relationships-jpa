package com.example.entityrelationshipsjpa.service;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import com.example.entityrelationshipsjpa.repository.NoteRepository;
import com.example.entityrelationshipsjpa.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
