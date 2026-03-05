package com.serverest.builder;

import com.github.javafaker.Faker;
import com.serverest.model.User;

import java.util.UUID;

public class UserBuilder {
    private static final Faker faker = new Faker();
    
    private String nome;
    private String email;
    private String password;
    private String administrador;

    private UserBuilder() {
        this.nome = faker.name().fullName();
        this.email = faker.internet().emailAddress();
        this.password = faker.internet().password();
        this.administrador = "false";
    }

    public static UserBuilder anUser() {
        return new UserBuilder();
    }

    public UserBuilder withAdmin(boolean isAdmin) {
        this.administrador = String.valueOf(isAdmin);
        return this;
    }

    public UserBuilder withEmailRandom() {
        // Ensuring uniqueness with UUID suffix
        String[] parts = this.email.split("@");
        this.email = parts[0] + "+" + UUID.randomUUID().toString().substring(0, 8) + "@" + parts[1];
        return this;
    }

    public UserBuilder withInvalidEmail() {
        this.email = "invalid_email_format";
        return this;
    }

    public User build() {
        if (!this.email.contains("+")) {
             withEmailRandom(); // Default to random unique if not set otherwise
        }
        return User.builder()
                .nome(nome)
                .email(email)
                .password(password)
                .administrador(administrador)
                .build();
    }
}