package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private String email;
    private String name;
    private Long id;
    private LocalDate dateOfBirth;
    private Gender gender;
}
