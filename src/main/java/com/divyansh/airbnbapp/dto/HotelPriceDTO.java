package com.divyansh.airbnbapp.dto;

import com.divyansh.airbnbapp.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDTO {
    private Hotel hotel;
    private Double price;
}
