package org.example;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.time.LocalDate;

@JsonSerialize // Jackson annotation
@Entity // Defines database entity
@Table(name = "Trainees") //Specifies table name
public class Trainee {

    //TODO: remove auto generated ID in production
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // Auto-generate IDs
    private int id;

    private String firstName;
    private String lastName;
    private String email;
    @Column(name = "endDate") private LocalDate endDate;

//    //Constructor with ID
//    public Trainee(int id, String firstName, String lastName, String email, LocalDate endDate) {
//        this.id = id;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.endDate = endDate;
//    }

    //Constructor without ID (since id is auto-generated)
    public Trainee(String firstName, String lastName, String email, LocalDate endDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.endDate = endDate;
    }

    // No-argument constructor (required by JPA)
    public Trainee() {}

}