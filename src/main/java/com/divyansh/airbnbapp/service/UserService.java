package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.OAuthResponseDTO;
import com.divyansh.airbnbapp.dto.ProfileUpdateRequestDTO;
import com.divyansh.airbnbapp.dto.UserDTO;
import com.divyansh.airbnbapp.entity.User;

public interface UserService {

    OAuthResponseDTO loginOrCreateGoogleUser(String email, String name);

    User getUserById(Long id);

    User save(User user);

    void updateProfile(ProfileUpdateRequestDTO profileUpdateRequestDTO);

    UserDTO getMyProfile();
}
