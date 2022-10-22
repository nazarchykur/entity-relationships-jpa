package com.example.entityrelationshipsjpa;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import com.example.entityrelationshipsjpa.service.NoteService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EntityRelationshipsJpaApplication {

    public static void main(String[] args) {
//        SpringApplication.run(EntityRelationshipsJpaApplication.class, args);
        ConfigurableApplicationContext context = SpringApplication.run(EntityRelationshipsJpaApplication.class, args);

        NoteService noteService = context.getBean(NoteService.class);
        Person person = new Person();
        person.setFirstName("Jane");
        person.setLastName("Smith");
        person.setEmail("janesmith@gmail.com");

        Note note = new Note("some note...");
        note.setPerson(person); // запишуться дані у колонці person_id (FK) як id цього Person в БД

        noteService.saveNoteWithPerson(note, person);
        
        /*
            тут ми спочатку створюємо Person 
            потім створюємо Note і передаємо цього Person у поле на стороні дочірньої Entity
            
            все відпрацює коректно = відбудуться 2 інсерти в БД з потрібними даними
         */
    }
}

/*

    про звязок 1-m  | One-to-Many  |  1:N 
    
    !!! у пропертіс встановлено ddl-auto: create
        щоб кожного разу наглядно показати скільки створиться таблиць і їх констреінти
        для кожного нового прикладу можна дропнути таблиці
    
    отже, з точки БД у нас є завжди одна ситуація:
        - є дві таблиці:
            - парент таблиця зі своїми даними
            - чайлд таблиця зі своїми даними + ще одна колонка = FK як посилання на колонку PK парент таблиці
        
    
    One-to-Many:
    
        order       items           - must be assigned to the order
        video       comments        - must be assigned to the video
        account     cards           - must be assigned to the account  
        team        players         - can exist without a team
        user        applications    - can be anonymous
        
        
        == Person == 
    ╔════╤════════════╤═════╗
    ║ id │ name       │ age ║
    ╠════╪════════════╪═════╣
    ║ 1  │ John Doe   │ 32  ║=-------------------|
    ╟────┼────────────┼─────╢                    |
    ║ 2  │ Jane Smith │ 27  ║=-----------------------|
    ╚════╧════════════╧═════╝                    |   |
                                                 |   |
        == Note ==                               |   |
    ╔════╤════════════╤════════╤═════════════╗   |   |
    ║ id │ date       │ amount │  person_id  ║   |   |
    ╠════╪════════════╪════════╪═════════════╣   |   |
    ║ 1  │ #Tue 2 ... │ 2      │ 1           ║<--|   |
    ╟────┼────────────┼────────┼─────────────╢   |   |
    ║ 2  │ #Tue 2 ... │ 1      │ 1           ║<--|   |
    ╟────┼────────────┼────────┼─────────────╢       |
    ║ 3  │ #Sun 9 ... │ 3      │ 2           ║<------|
    ╚════╧════════════╧════════╧═════════════╝    
    
    
    
    наприклад у нас є 2 Entity = Person, Note
    
    для Hibernate вказавши цей зв'язок тільки з одного боку, чи тільки з другого, чи з обох = 
    це все буде мати різний варіант створення і відображення наших Entity
    
    отже з точки Java за допомогою Hibernate може показати 3 варіанти:
     - 1) (ЦЕЙ ВАРІАНТ НЕ ДОБРИЙ на стороні БД) вказати тільки зі сторони Person
                @OneToMany
                private List<Note> notes = new ArrayList<>();
                
         тобто ми зі сторони парентової таблиці вказуємо цей зв'язок    (unidirectional mapping)  
        = згенерується 3 таблички:
            - notes
            - persons
            - persons_notes       (створиться зайва таблиця для цього зв'язку)
            
            
     - 2) (ЦЕЙ ВАРІАНТ НЕ ДОБРИЙ на стороні JAVA у подальшій роботі з цими Entities) вказати тільки зі сторони Notes як чайлд таблиці   (unidirectional mapping)  
                 @ManyToOne
                 // @JoinColumn(name = "person_id") // по замовчуванню так і створить
                 private Person person;  
                   
        = згенерується 2 таблички:
            - notes  де будуть 3 колонки = 2 поля від notes + 1 колонка FK як посилання на табл. persons(id) PK
            - persons 
            
        тобто з точки БД все працює добре і ефективно, але не зручно працювати зі сторони JAVA              
        бо, наприклад, ми дістанемо якогось Person, але в нас немає доступу до його Note
 */