package com.example.entityrelationshipsjpa;

import com.example.entityrelationshipsjpa.entity.Note;
import com.example.entityrelationshipsjpa.entity.Person;
import com.example.entityrelationshipsjpa.repository.NoteRepository;
import com.example.entityrelationshipsjpa.repository.PersonRepository;
import com.example.entityrelationshipsjpa.service.NoteService;
import com.example.entityrelationshipsjpa.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
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

        PersonService personService = context.getBean(PersonService.class);
        personService.getAllNotesByPerson(1L);
        
        // 1. save new Person with new Notes using cascade
        /*
             без використання хелпер методів нам потрібно було б завжди самим синхронізовувати ці дані
             тобто з Парент сторони вказувати які дані добавити у список цієї дочірньої Entity
             і зі сторони Чайлд (дочірньої Entity) просетати дані щодо Парент (осноної Entity)
             
                            Parent parent = new Parent();
                            ...
                            Child c1 = new Child();
                            ...
                            c1.setParent(parent);
                            
                            List<Child> children = new ArrayList<Child>();
                            children.add(c1);
                            parent.setChildren(children);
                            
                            session.save(parent);
                    
                   
               зберігаючи Person, ми за допомогою наших хелпер методів зберігаємо Note, щоб цей звязок був синхронізований
                і тому дані коректно збережуться у БД
                в даному патерні, коли налаштовуємо цей bidirectional association, тобто ми лінкуємо обидві сторони, шоб 
                кожна сторона коректно посилалася у цьому звя'зку Parent -> Child
                і зберігання / оновлення / видалення коректно спрацьовували, коли ми праюємо у JAVA з цими Entities (Parent, Child)
                І код стає таким:      
                            Parent parent = new Parent();
                            ...
                            Child c1 = new Child();
                            ...
                            
                            parent.addToChildren(c1);
                            
                            session.save(parent);
         */
        
        Person person1 = new Person();
        person1.setFirstName("Leo");
        person1.setLastName("Turtle");
        person1.setEmail("leoT@gmail.com");

        Note note1 = new Note("leo note 1");
        
        personService.saveNewPersonWithNewNotes(person1, note1);
        
        // 2. зберегти new Note до існуючого Person (select = 1, insert = 1)

        Note note2 = new Note("new note 2");
        noteService.saveNewNoteToExistingPerson(1L, note2);
        
        /*
                +----+--------------+-----------+
                | id | body         | person_id |
                +----+--------------+-----------+
                | 1  | some note... | 1         |
                +----+--------------+-----------+
                | 2  | leo note 1   | 2         |
                +----+--------------+-----------+
                | 3  | new note 2   | 1         |   <= новий запис
                +----+--------------+-----------+
         */


        // 3. створити new Note і додати її до існуючого Person (select = 1, insert = 1)
        /*
            тут ми не викликаємо метод save ...
            бо викликаючи  personRepository.findById(personId), щоб дістати Person, то 
            Hibernate уже відкриє сесію і буде знати про Person
            і як тільки ми захочемо додати Note через ці хелпер методи
                    person.addNote(note);
            то Hibernate побачить, що ця колекція змінилася, бо cascade стосується дочірньої Entity
            і відбудеться збереження цієї Note, тобто insert = 1 у дочірню таблицю
         */

        Note note3 = new Note("new note 3");
        personService.addNewNotes(1L, note3);
        
        /*
                +----+--------------+-----------+
                | id | body         | person_id |
                +----+--------------+-----------+
                | 1  | some note... | 1         |
                +----+--------------+-----------+
                | 2  | leo note 1   | 2         |
                +----+--------------+-----------+
                | 3  | new note 2   | 1         |
                +----+--------------+-----------+
                | 4  | new note 3   | 1         |   <= новий запис
                +----+--------------+-----------+
         */

        // 4. зберегти new Note до існуючого Person по personId без загрузки Person у сесію,
        // тобто без вигрузки з БД (select = 0, insert = 1)
        // тобто відбудеться тільки один INSERT в БД щодо збереження нової нотатки

        Note note4 = new Note("new note 4");
        personService.saveNewNoteUsingProxy(2L, note4);
        
         /*
                +----+--------------+-----------+
                | id | body         | person_id |
                +----+--------------+-----------+
                | 1  | some note... | 1         |
                +----+--------------+-----------+
                | 2  | leo note 1   | 2         |
                +----+--------------+-----------+
                | 3  | new note 2   | 1         |
                +----+--------------+-----------+
                | 4  | new note 3   | 1         | 
                +----+--------------+-----------+
                | 5  | new note 4   | 2         |   <= новий запис
                +----+--------------+-----------+
         */
        /*
                якщо передати неіснуючий person_id, то впаде через Exception:
                
                could not execute statement; SQL [n/a]; constraint [notes_persons_fk]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement
                ...
                Caused by: org.postgresql.util.PSQLException: ERROR: insert or update on table "notes" violates foreign key constraint "notes_persons_fk"
         */

        NoteRepository noteRepository = context.getBean(NoteRepository.class);
        log.info("get Notes by personId = " + 1 + " :" + noteRepository.findAllByPersonId(1L));
        // get Notes by personId = 1 :[Note(id=1, body=some note...), Note(id=3, body=new note 2), Note(id=4, body=new note 3)]

//        PersonRepository personRepository = context.getBean(PersonRepository.class);
//        log.info("get Person => " + personRepository.findById(1L));
        // failed to lazily initialize a collection of role: com.example.entityrelationshipsjpa.entity.Person.notes, could not initialize proxy - no Session
        // впаде через помилку = пізніше розглянемо як ефективно працювати з підгрузкою потрібного поля 

        Person person2 = personService.findBy(1L);
        log.info("get Person => " + person2);
//        get Person => Person(id=1, firstName=Jane, lastName=Smith, email=janesmith@gmail.com, 
//        notes=[Note(id=1, body=some note...), Note(id=3, body=new note 2), Note(id=4, body=new note 3)])
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
    це все буде мати різний варіант створення і відображення наших Entity у БД
    
    отже, з точки Java за допомогою Hibernate може показати 3 варіанти:
     - 1) (ЦЕЙ ВАРІАНТ НЕ ДОБРИЙ на стороні БД) вказати тільки зі сторони Person (основної таблиці)
                @OneToMany
                private List<Note> notes = new ArrayList<>();
                
         тобто ми зі сторони парентової таблиці вказуємо цей зв'язок    (unidirectional mapping)  
        = згенерується 3 таблички:
            - notes
            - persons
            - persons_notes       (створиться зайва таблиця для цього зв'язку)
            
            
     - 2) (ЦЕЙ ВАРІАНТ НЕ ДОБРИЙ на стороні JAVA у подальшій роботі з цими Entities) вказати тільки зі сторони Notes як чайлд таблиці   (unidirectional mapping)  
                 @ManyToOne
                 // @JoinColumn(name = "person_id") // по замовчуванню так і створить цей FK
                 private Person person;  
                   
        = згенерується 2 таблички:
            - notes  де будуть 3 колонки = 2 поля від notes + 1 колонка FK як посилання на табл. persons(id) PK
            - persons 
            
        тобто з точки БД все працює добре і ефективно, але не зручно працювати зі сторони JAVA              
        бо, наприклад, ми дістанемо якогось Person, але в нас немає доступу до його Note
        
      - 3) bidirectional mapping 
            в більшості або навіть в усіх випадках потрібно використовувати цей варіант, де:
            Person:
                    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
                    private List<Note> notes = new ArrayList<>();
                    
            Note:
                    @ManyToOne
                    @JoinColumn(name = "person_id")
                    private Person person;        
      
      коли ми проставимо bidirectional зв'язок між цими Entities, то 
      Hibernate всерівно для контролю цієї залежності використовує мапінг з одного боку
      
      !!! але тут є кілька нюансів, які слід пам'ятати:
      
       - 1) FetchType (LAZY, EAGER)
              зв'язки:  
                    - ..ToMany - by default has FetchType.LAZY
                    - ..ToOne  - by default has FetchType.EAGER    
        
        - 2) каскадні операції - це операції, які поширюються на дочірню сутність.
            у цих зв'язках визначається, що робити з дочірньою сутністю, якщо ми будемо
            добавляти / оновлювати / видаляти основну сутність
            
            При видаленні якогось топіка треба видаляти з бази його та його всі коментарі, 
            при збереженні топіка – зберігати його і його коментарі,
            а при оновленні топіка - оновити його і всі його коментарі.
              
            cascade
            Є кілька типів каскадних операцій:
                - CascadeType.ALL
                - CascadeType.PERSIST
                - CascadeType.MERGE
                - CascadeType.REMOVE
                - CascadeType.REFRESH
                - CascadeType.DETACH
          
        - 3) orphanRemoval  - стосується видалення елементів із колекції.
            наприклад, видалення коментаря зі списку коментарів Топіка.
            
            Щоб зрозуміти зміст налаштування orphanRemoval, треба уявити, що теоретично може матися на увазі 
            під видаленням коментаря зі списку коментарів топіка.

            Очевидно це означає, що цей топік більше не має коментаря.
            1. Але чи він залишається взагалі в БД, тобто чи можна його вивести в загальному списку коментарів усіх топіків?
            2. Чи коментар видаляється з бази?
            
            За ці два варіанти і відповідає orphanRemoval
            
           = Якщо orphanRemoval=true, то при видаленні коментаря зі списку коментарів топіка коментар видаляється з бази.
           = Якщо orphanRemoval=false, то при видаленні коментаря зі списку, в базі коментар залишається. 
             Просто його зовнішній ключ ( comment.topic_id) обнулюється, і більше коментар не посилається на топік.
        
        - 4) helper methods = ці методи потрібні для коректної роботи цього bidirectional relationship
               тобто коли ми працюємо з основною Entity у нас є список елементів цієї дочірньої Entity
                 і при добавленні у список / видаленні елемента зі списку цієї дочірньої Entity нам 
                 потрібно передати цей FK або обНУЛити це посилання, щоб завжди СИНХРОНІЗОВУВАТИ цей звязок
                 
            
            !!! кілька останніх пунктів дозволять працювати коректно.
                Так дані залишаються узгодженими. Hibernate автоматично не може забезпечити узгодженість двосторонніх 
                (bidirectional) відносин, тому і треба це робити самостійно за допомогою хелпер методів.  
                
        - 5) можна використати FetchType.LAZY for @ManyToOne association
        
        - 6) Think twice before using CascadeType.Remove
        
        - 7) implementing toString is bad from a performance perspective.
                метод toString може використовувати будь-які базові атрибути сутності (які потрібні для ідентифікації певної сутності при логуванні),
                якщо базові атрибути витягуються під час завантаження сутності з бази даних, бо Hibernate дозволяє ліниво завантажувати ці атрибути.
                
                
        - 8) Equals and hashCode
        
            According to Java specification, a good equals implementation must have the following properties:
                - reflexive = Рефлексивність
                    для будь-якого заданого значення x, вираз x.equals(x) має повертати true.
                    при умові, що   x != null
                
                - symmetric = Симетричність
                    для будь-яких заданих значень x і y, x.equals(y) має повертати true тільки в тому випадку,
                    коли y.equals(x) true.    
           
                 - transitive = Транзитивність
                    для будь-яких заданих значень x, y і z, якщо x.equals(y) повертає true та y.equals(z) повертає true,
                    то x.equals(z) має повернути значення true.

                - consistent = Узгодженість
                    для будь-яких заданих значень x та y повторний виклик x.equals(y) повертатиме значення попереднього 
                    виклику цього методу за умови, що поля, які використовуються для порівняння цих двох об'єктів, 
                    не змінювалися між викликами.
                    
                - Порівняння null
                    для будь-якого заданого значення x виклик x.equals(null) повинен повертати false.
  =======================================================================================================================
      !!! Висновок: для відображення many-to-one / one-to-many association
        потрібно використовувати Bidirectional associations = BEST PRACTICE:
        
        1. Person (Parent side) -> Note (Child side)
        
        2. Note (Child side):
            - FetchType.LAZY - щоб уникнути ситуації N+1
            
                @ManyToOne(optional = false, fetch = FetchType.LAZY) // optional = false - чи може існувати Note без Person (на цій колонці буде NOT NULL) | по дефолту optional = true
                @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "notes_persons_fk")) // foreignKey = самому вказати ім'я
                private Person person; 
            
        3. Person (Parent side) + helpers methods for synchronization BOTH sides:
            - завжди пам`ятати чи потрібен CascadeType.REMOVE
        
                    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
                    private List<Note> notes = new ArrayList<>();
                    
                    public void addNote(Note note) {
                        note.setPerson(this);
                        notes.add(note);
                    }
                    
                    public void removeNote(Note note) {
                        note.setPerson(null);
                        notes.remove(note);
                    }
                    
         4. завжди пам'ятати за 
             - ToString
             - Equals and HashCode             
 */

/*
    LazyInitializationException – What it is and the best way to fix it
    
    https://thorben-janssen.com/lazyinitializationexception/
    
    https://vladmihalcea.com/the-hibernate-enable_lazy_load_no_trans-anti-pattern/
    
    
    Виняток LazyInitializationException є одним із найпоширеніших винятків під час роботи зі Hibernate. 
    Є кілька простих способів як це виправити, але також можна знайти безліч поганих порад:
        - деякі виправлення часто замінюють виняток прихованою проблемою, яка спричиняє проблеми у PROD. 
        - деякі з них спричиняють проблеми з самою продуктивністю
        - інші можуть створювати суперечливі результати.
        
    Hibernate створює виключення LazyInitializationException, коли йому потрібно ініціалізувати ліниво отриману асоціацію 
    з іншою сутністю без активного контексту сеансу. Зазвичай це відбувається, якщо ви намагаєтесь використати 
    неініціалізовану асоціацію у своїй клієнтській програмі чи веб-рівні.
    
    Наприклад, Author + Books (One-to-Many + Many-to-One). також це стосується і One-to-One, де ми поставили FetchType.LAZY + визначили CascadeType
    
    Отже, наприклад ми дістали по якомусь критерію деякий список авторів, а потім у кожного автора дістаємо його книги
    
                List<Author> authors = query.getResultList();
                em.getTransaction().commit();         // десь у коді закрили транзакцію, або це наприклад був 
                em.close();                           // якийсь метод сервісу, після виклику якого, сесія також закрилася
                
                // опісля хочемо отримати книги цих авторів для наступної операції
                for (Author author : authors) {
                    List<Book> books = author.getBooks();
                    log.info("... the next line will throw LazyInitializationException ...");
                    books.size();
                 }
                 
             Запит до бази даних повертає сутність автора з ліниво отриманою асоціацією з книгами, написаними цим автором. 
             Hibernate ініціалізує атрибути книг за допомогою власної реалізації List , яка обробляє відкладене завантаження. 
             Коли ви намагаєтесь отримати доступ до елемента в цьому списку або викликаєте метод, який працює з його елементами, 
             реалізація списку Hibernate визнає, що активний сеанс недоступний, і викидає LazyInitializationException .    
             
             
             Як НЕ виправляти LazyInitializationException:
             
             - Не використовуйте FetchType.EAGER
                    Деякі розробники пропонують змінити FetchType асоціації на EAGER. 
                    Звичайно, це виправляє LazyInitializationException, 
                    але створює проблеми з продуктивністю, які виявлятимуться у PROD.
                    
                    Якщо ви встановите FetchType на EAGER , Hibernate завжди отримуватиме асоціацію, навіть якщо ви не 
                    будете її використовувати. Це, очевидно, спричиняє накладні витрати, які сповільнюють вашу програму. 
                    Але це стає ще гірше, якщо ви не використовуєте метод EntityManager.find і не посилаєтеся на асоціацію
                    через JOIN FETCH . 
                    Hibernate виконує додатковий запит для отримання асоціації. Це часто призводить до проблеми вибору n+1 , 
                    яка є найпоширенішою причиною проблем із продуктивністю.

                    Тому, будь ласка, не використовуйте FetchType.EAGER, ви завжди повинні віддавати перевагу FetchType.LAZY .
                    
                    
              - Уникайте антишаблону Open Session in View   
              
                    Під час використання антипаттеру Open Session in View ви відкриваєте та закриваєте EntityManager 
                    або Hibernate Session у своєму шарі перегляду (view layer). Потім ви викликаєте рівень сервісу, 
                    який відкриває та фіксує транзакцію бази даних. Оскільки сеанс все ще відкритий після того, як рівень 
                    сервісу повернув сутність, рівень перегляду може ініціалізувати ліниво отриману асоціацію.
                    
                    Але після того, як сервісний рівень зафіксував транзакцію бази даних, активної транзакції немає. 
                    Через це Hibernate виконує кожну інструкцію SQL, ініційовану шаром перегляду, у режимі автоматичної 
                    фіксації. Це збільшує навантаження на сервер бази даних, оскільки він повинен обробляти додаткову 
                    транзакцію для кожного оператора SQL. Наприкінці кожної з цих транзакцій база даних має записати 
                    журнал транзакцій на диск, що є дорогою операцією.
                    
                    Це також може дати суперечливі результати, оскільки зараз ви використовуєте 2 або більше незалежних 
                    транзакцій. У результаті ліниво отримана асоціація може повертати дані, відмінні від даних, які 
                    ваш сервісний рівень використовував для виконання бізнес-логіки. Потім ваш рівень перегляду 
                    представляє обидві дані разом, і може здатися, що ваша програма керує неузгодженими даними.
                    
                    На жаль, Spring Boot за замовчуванням використовує антишаблон Open Session in View. 
                    Він реєструє лише попереджувальні повідомлення.
                            2020-03-06 16:18:21.292  WARN 11552 - – [  restartedMain] JpaBaseConfiguration$JpaWebConfiguration : 
                                spring.jpa.open-in-view is enabled by default. Therefore, database queries may be 
                                performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning

                    Ви можете деактивувати його, встановивши для параметра spring.jpa.open-in-view у вашому файлі 
                    application.properties значення false .

            
            - Не використовуйте hibernate.enable_lazy_load_no_trans
            
                        spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
                        
                  Цей параметр повідомляє Hibernate відкрити тимчасовий сеанс, якщо активний сеанс недоступний для 
                  ініціалізації ліниво отриманого зв’язку. Це збільшує кількість використаних підключень до бази даних, 
                  транзакцій бази даних і загальне навантаження на вашу базу даних.  
                  
                  
      ----------------------------------------------------------------------------------------------------------------------------------------            
            =  Як виправити LazyInitializationException
                    
                     Правильний спосіб виправити LazyInitializationException — це отримати всі необхідні асоціації в 
                     межах рівня обслуговування. Найкращий варіант для цього — завантажити сутність з усіма необхідними 
                     асоціаціями в одному запиті. Або ви можете використовувати проєкцію DTO, яка не підтримує відкладене 
                     завантаження та потребує повної ініціалізації, перш ніж повертати її клієнту.    
                     
                     
                - Ініціалізація асоціацій за допомогою пропозиції LEFT JOIN FETCH
                    
                          Найпростіший спосіб завантажити сутність із усіма необхідними асоціаціями — це виконати запит 
                          JPQL або Criteria Query з одним або декількома пропозиціями LEFT JOIN FETCH. 
                          Це вказує Hibernate не лише отримати сутність, на яку посилається в проекції, але й отримати 
                          всі пов’язані сутності, на які є посилання в реченні LEFT JOIN FETCH.
                          
                          
                                    EntityManager em = emf.createEntityManager();
                                    em.getTransaction().begin();
                                     
                                    TypedQuery<Author> q = em.createQuery("SELECT a FROM Author a LEFT JOIN FETCH a.books", Author.class);
                                    List<Author> authors = q.getResultList();
                                     
                                    em.getTransaction().commit();
                                    em.close();
                                     
                                    for (Author a : authors) {
                                        log.info(a.getName() + " wrote the books "
                                            + a.getBooks().stream().map(b -> b.getTitle()).collect(Collectors.joining(", "))
                                        );
                                    }
                          
                          Запит вибирає сутності Author, а LEFT JOIN FETCH повідомляє Hibernate також отримати пов’язані 
                          сутності Book. Як ви можете бачити в створеному операторі SQL, Hibernate не лише об’єднує 
                          2 відповідні таблиці в FROM, але також додає всі стовпці, зіставлені сутністю Book, до SELECT.
                          
                                    select
                                        author0_.id as id1_0_0_,
                                        books1_.id as id1_2_1_,
                                        author0_.name as name2_0_0_,
                                        author0_.version as version3_0_0_,
                                        books1_.author_id as author_i7_2_1_,
                                        books1_.authorEager_id as authorEa8_2_1_,
                                        books1_.publisher as publishe2_2_1_,
                                        books1_.publishingDate as publishi3_2_1_,
                                        books1_.sells as sells4_2_1_,
                                        books1_.title as title5_2_1_,
                                        books1_.version as version6_2_1_,
                                        books1_.author_id as author_i7_2_0__,
                                        books1_.id as id1_2_0__ 
                                    from
                                        Author author0_ 
                                    left outer join
                                        Book books1_ 
                                            on author0_.id=books1_.author_id
        
        
                          
                 -  Використовуйте @NamedEntityGraph для ініціалізації асоціації     
                       
                       Ви можете зробити те саме за допомогою @NamedEntityGraph. Основна відмінність полягає в тому, 
                       що визначення графіка не залежить від запиту. Це дає змогу використовувати той самий запит із 
                       різними графіками або використовувати той самий графік із різними запитами.
                       
                       ...
                       
                
                 - Використання проекції DTO
                 
                       альтернатива, яка навіть краще підходить для всіх операцій читання. 
                       DTO забезпечують значно кращу продуктивність, якщо ви не хочете змінювати отриману інформацію.  
                       
                       У таких ситуаціях ви можете використовувати вираз конструктора, щоб сказати Hibernate створити 
                       екземпляр об’єкта DTO для кожного запису в наборі результатів.
                             
                                EntityManager em = emf.createEntityManager();
                                em.getTransaction().begin();
                                 
                                TypedQuery<AuthorDto> q = em.createQuery(
                                        "SELECT new org.gratejava.dto.AuthorDto(a.name,b.title) FROM Author a JOIN a.books b",
                                        AuthorDto.class);
                                List<AuthorDto> authors = q.getResultList();
                                 
                                em.getTransaction().commit();
                                em.close();
                                 
                                for (AuthorDto author : authors) {
                                    log.info(author.getName() + " wrote the book " + author.getBookTitle());
                                }
                                
                        Потім Hibernate генерує інструкцію SQL, яка вибирає лише ті стовпці, які відображаються атрибутами, 
                        на які ви посилаєтеся під час виклику конструктора. Це часто зменшує кількість вибраних стовпців 
                        і ще більше покращує продуктивність.
                        
                                select
                                    author0_.name as col_0_0_,
                                    books1_.title as col_1_0_ 
                                from
                                    Author author0_ 
                                inner join
                                    Book books1_ 
                                        on author0_.id=books1_.author_id
                                        
                                        
    ====================================================
    
    Висновок
       
        Є лише 2 хороших рішення LazyInitializationException проблеми:
        
        1. Ви ініціалізуєте всі необхідні асоціації, коли завантажуєте сутність за допомогою LEFT JOIN FETCH 
           або @NamedEntityGraph або API EntityGraph.
           
        2. Ви використовуєте проекцію DTO замість сутностей. DTO не підтримують відкладене завантаження, і вам потрібно 
           отримати всю необхідну інформацію на рівні сервісу.                                    
                                
 */