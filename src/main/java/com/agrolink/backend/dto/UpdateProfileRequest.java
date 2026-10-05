package com.agrolink.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProfileRequest {

    @JsonAlias({"fullName", "userName", "username", "Name"})
    private String name;

    @JsonAlias({"phoneNumber", "mobile", "mobileNumber", "contactNo", "Phone"})
    private String phone;

    @JsonAlias({"location", "Address"})
    private String address;

    @JsonAlias({"lat"})
    private Double latitude;

    @JsonAlias({"lng", "lon"})
    private Double longitude;
}
