package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.BookingResponseDTO;
import com.divyansh.airbnbapp.dto.ProfileUpdateRequestDTO;
import com.divyansh.airbnbapp.dto.UserDTO;
import com.divyansh.airbnbapp.service.BookingService;
import com.divyansh.airbnbapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDTO profileUpdateRequestDTO){
        userService.updateProfile(profileUpdateRequestDTO);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }
    @PatchMapping("promote-to-host")
    public ResponseEntity<?> promoteUser(){
        return ResponseEntity.ok(userService.promoteUser());
    }
}
