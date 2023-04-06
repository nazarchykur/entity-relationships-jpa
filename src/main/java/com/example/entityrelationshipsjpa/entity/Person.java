package com.example.entityrelationshipsjpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // означає, що ця колонка має бути NOT NULL | працює тільки при генерації БД
    private String firstName;

    @Column(nullable = false) // означає, що ця колонка має бути NOT NULL | працює тільки при генерації БД
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Note> notes = new ArrayList<>();
    
    public void addNote(Note note) {
        note.setPerson(this);
        notes.add(note);
    }
    
    public void removeNote(Note note) {
        note.setPerson(null);
        notes.remove(note);
    }
}

