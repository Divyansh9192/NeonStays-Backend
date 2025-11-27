package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.enums.Gender;
import com.divyansh.airbnbapp.entity.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDTO {
    private String email;
    private String name;
    private Long id;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Set<Role> roles;
}
