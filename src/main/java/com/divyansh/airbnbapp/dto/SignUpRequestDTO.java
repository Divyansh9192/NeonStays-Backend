package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignUpRequestDTO {
    private String email;
    private String password;
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
