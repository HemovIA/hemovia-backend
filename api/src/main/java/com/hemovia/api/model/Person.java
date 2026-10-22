package com.hemovia.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "person")
public class Person {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotBlank @Column(nullable = false) private String name;
    @NotBlank @Email @Column(nullable = false) private String email;
    @NotBlank @Column(name = "phone_number", nullable = false) private String phoneNumber;

    public Person() { }
    public Person(UUID id, String name, String email, String phoneNumber) {
        this.id = id; setName(name); setEmail(email); setPhoneNumber(phoneNumber);
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = requiredText(name, "name"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = requiredText(email, "email"); }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = requiredText(phoneNumber, "phoneNumber"); }

    private static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
