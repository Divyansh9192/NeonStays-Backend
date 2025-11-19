package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthResponseDTO {
    private User user;
    private String[] tokens;
}
