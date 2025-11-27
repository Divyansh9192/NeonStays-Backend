package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.OAuthResponseDTO;
import com.divyansh.airbnbapp.dto.ProfileUpdateRequestDTO;
import com.divyansh.airbnbapp.dto.UserDTO;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.entity.enums.Role;
import com.divyansh.airbnbapp.exception.ResourceNotFoundException;
import com.divyansh.airbnbapp.repository.UserRepository;
import com.divyansh.airbnbapp.security.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.divyansh.airbnbapp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JWTService jwtService;

    @Override
    public OAuthResponseDTO loginOrCreateGoogleUser(String email, String name) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user==null) {
            String randomPassword = UUID.randomUUID().toString();
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRoles(Set.of(Role.GUEST));
            user.setPassword(randomPassword);
            userRepository.save(user);
        }
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return new OAuthResponseDTO(user,arr);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found With Id:"+id));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDTO profileUpdateRequestDTO) {
        User user = getCurrentUser();

        if (profileUpdateRequestDTO.getDateOfBirth()!=null){
            user.setDateOfBirth(profileUpdateRequestDTO.getDateOfBirth());
        }
        if (profileUpdateRequestDTO.getGender()!=null){
            user.setGender(profileUpdateRequestDTO.getGender());
        }
        if (profileUpdateRequestDTO.getName()!=null){
            user.setName(profileUpdateRequestDTO.getName());
        }

        userRepository.save(user);
    }

    @Override
    public UserDTO getMyProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    public Void promoteUser() {
        User user = getCurrentUser();
        user.setRoles(Set.of(Role.HOTEL_MANAGER,Role.GUEST));
        userRepository.save(user);
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         return userRepository.findByEmail(username).orElse(null);
    }

}
