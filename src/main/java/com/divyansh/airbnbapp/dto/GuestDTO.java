package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDTO {

    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
