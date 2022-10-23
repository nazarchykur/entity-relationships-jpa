package com.example.entityrelationshipsjpa.service;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import com.example.entityrelationshipsjpa.repository.PersonRepository;
import com.example.entityrelationshipsjpa.repository.PersonRepositoryCustom;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    
    private final PersonRepositoryCustom personRepositoryCustom;

    @Transactional
    public void getAllNotesByPerson(Long personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalStateException("Person by this id=" + personId + " does not exist"));

        log.info("Get all notes by person using our mapping instead of manually writing query");

        person.getNotes().forEach(System.out::println);
    }

    @Transactional
    public void saveNewPersonWithNewNotes(Person person, Note note) {
        // 1. save new Person with new Notes using cascade
        // зберігаючи Person, ми за допомогою наших хелпер методів зберігаємо Note, щоб цей звязок був синхронізований
        // і тому дані коректно збережуться у БД
        log.info("1. save new Person with new Notes using cascade = saveNewPersonWithNewNotes");
        
        person.addNote(note);
        personRepository.save(person);
    }

    @Transactional
    public void addNewNotes(Long personId, Note note) {
        // 3. створити new Note і додати її до існуючого Person (select = 1, insert = 1)
        log.info("3. create a new Note and add it to existing Person (select = 1, insert = 1)");

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalStateException("Person by this id=" + personId + " does not exist"));

        person.addNote(note);
    }
    
    @Transactional
    public void saveNewNoteUsingProxy(Long personId, Note note) {
        // 4. зберегти new Note до існуючого Person по personId без загрузки Person у сесію,
        // тобто без вигрузки з БД (select = 0, insert = 1)
        log.info("4. save a new Note to existing Person by personId using Proxy = without loading Person to Hibernate Session = (select = 0, insert = 1)");
        personRepositoryCustom.saveNewNoteUsingProxy(personId, note);
    }
}
