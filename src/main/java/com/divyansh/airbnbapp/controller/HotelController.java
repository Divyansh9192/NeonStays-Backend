package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.HotelDTO;
import com.divyansh.airbnbapp.dto.HotelReportDTO;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.service.BookingService;
import com.divyansh.airbnbapp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {
    private final HotelService hotelService;
    private final BookingService bookingService;
    @PostMapping
    public ResponseEntity<HotelDTO> createNewHotel(@RequestBody HotelDTO hotelDTO){
        log.info("Attempting to create a new hotel with name"+hotelDTO.getName());
        HotelDTO hotelDTO1 = hotelService.createNewHotel(hotelDTO);
        return new ResponseEntity<>(hotelDTO1, HttpStatus.CREATED);
    }
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable Long hotelId){
        HotelDTO hotelDTO = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDTO);
    }

    @GetMapping
    public ResponseEntity<List<HotelDTO>> getAllHotels(){
        List<HotelDTO> allHotels = hotelService.getAllHotel();
        return ResponseEntity.ok(allHotels);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDTO> updateHotelById(@PathVariable Long hotelId,@RequestBody HotelDTO hotelDTO){
        HotelDTO hotelDTO1 = hotelService.updateHotelById(hotelId,hotelDTO);
        return ResponseEntity.ok(hotelDTO);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    public ResponseEntity<Void> activatingHotelById(@PathVariable Long hotelId){
        hotelService.activateHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDTO> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false)LocalDate startDate,
                                                         @RequestParam(required = false)LocalDate endDate
                                                           ){
        if(startDate==null) startDate = LocalDate.now().minusMonths(1);
        if(endDate==null) endDate = LocalDate.now();
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }

}
