package com.divyansh.airbnbapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelSearchRequestDTO {
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;

    private Integer page=0;
    private Integer size=10;

    public HotelSearchRequestDTO(String checkIn, String checkOut, String city, int guests) {
        this.city = city;
        this.startDate = LocalDate.parse(checkIn);
        this.endDate = LocalDate.parse(checkOut);
        this.roomsCount = guests;
    }
}
