package com.jayasrimart.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing a user authentication login request.
 */
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    public UserLoginRequest() {
    }

    public UserLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
