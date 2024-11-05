package org.example;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDate;

@JsonSerialize // Jackson annotation
@Entity // Defines database entity
@Table(name = "Trainees") //Specifies table name
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // Auto-generate IDs
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    @Column(name = "endDate") private LocalDate endDate;

    //Constructor with ID
    public Trainee(int id, String firstName, String lastName, String email, LocalDate endDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.endDate = endDate;
    }

    //Constructor without ID (since id is auto-generated)
    public Trainee(String firstName, String lastName, String email, LocalDate endDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.endDate = endDate;
    }

    // No-argument constructor (required by JPA)
    public Trainee() {}

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for firstName
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter and Setter for lastName
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for endDate
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

