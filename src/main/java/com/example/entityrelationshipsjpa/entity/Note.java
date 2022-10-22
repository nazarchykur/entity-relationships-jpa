package com.example.entityrelationshipsjpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notes")
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String body;

    @ManyToOne
//    @JoinColumn(name = "person_id") // по замовчуванню так і створить
    private Person person;
    public Note(String body) {
        this.body = body;
    }
}
