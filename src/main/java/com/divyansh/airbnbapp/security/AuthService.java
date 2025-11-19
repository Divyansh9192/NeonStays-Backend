package com.divyansh.airbnbapp.security;


import com.divyansh.airbnbapp.dto.LoginDTO;
import com.divyansh.airbnbapp.dto.SignUpRequestDTO;
import com.divyansh.airbnbapp.dto.UserDTO;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.entity.enums.Role;
import com.divyansh.airbnbapp.exception.ResourceNotFoundException;
import com.divyansh.airbnbapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        User user = userRepository.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User is already present with the same email id");
        }
        User newUser = modelMapper.map(signUpRequestDTO, User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));

        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDTO.class);
    }
    public String[] login(LoginDTO loginDTO){

        try{
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getPassword()
        ));

        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Credentials");
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Auth Error");
        }
    }

    public String refreshToken(String refreshToken){
        Long id =jwtService.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("User not found with id:"+id));
        return jwtService.generateAccessToken(user);
    }

}
