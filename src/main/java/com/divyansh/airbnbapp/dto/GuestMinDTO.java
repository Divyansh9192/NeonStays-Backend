package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestMinDTO {
    private Long id;
    private String name;
    private Gender gender;
    private Integer age;
}
