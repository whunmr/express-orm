package com.thoughtworks.fixture;

import com.thoughtworks.Model;

public class User extends Model {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
