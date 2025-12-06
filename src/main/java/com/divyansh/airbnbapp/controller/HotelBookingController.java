package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.BookingRequestDTO;
import com.divyansh.airbnbapp.dto.GuestDTO;
import com.divyansh.airbnbapp.entity.Guest;
import com.divyansh.airbnbapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDTO> initialiseBooking(@RequestBody BookingRequestDTO bookingRequestDTO){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequestDTO));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDTO> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDTO> guestDTOList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guestDTOList));
    }
    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String,String>> initiatePayment(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));
    }
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/status")
    public ResponseEntity<Map<String,String>> getBookingStatus(@PathVariable Long bookingId){
        return ResponseEntity.ok(Map.of("status",bookingService.getBookingStatus(bookingId)));
    }
}
