package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingResponseDTO {
    private Long id;
    private Integer roomsCount;
    private Hotel hotel;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus bookingStatus;
    private BigDecimal amount;
    private Set<GuestMinDTO> guests;
}
