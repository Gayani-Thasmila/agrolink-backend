package com.agrolink.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @JsonAlias({"fullName", "userName", "username", "Name"})
    private String name;

    @JsonAlias({"mail", "emailAddress", "Email"})
    private String email;

    @JsonAlias({"pass", "Password"})
    private String password;

    @JsonAlias({"phoneNumber", "mobile", "mobileNumber", "contactNo", "Phone"})
    private String phone;

    @JsonAlias({"location", "Address"})
    private String address;

    @JsonAlias({"userType", "UserType"})
    private String role;
}
