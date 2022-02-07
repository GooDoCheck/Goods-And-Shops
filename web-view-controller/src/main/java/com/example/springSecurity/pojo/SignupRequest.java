package com.example.springSecurity.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
    private String name;
    private String surname;
    private String address;
    private String email;
    private String username;
    private String password;
    private Set<String> roles;
}
