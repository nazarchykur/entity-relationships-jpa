package com.example.entityrelationshipsjpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@ToString(exclude = "person")
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notes")
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false) // означає, що ця колонка має бути NOT NULL | працює тільки при генерації БД
    private String body;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // optional = false - чи може існувати Note без Person (на цій колонці буде NOT NULL) | по дефолту optional = true
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "notes_persons_fk")) // foreignKey = самому вказати ім'я
    private Person person;
    public Note(String body) {
        this.body = body;
    }
}
