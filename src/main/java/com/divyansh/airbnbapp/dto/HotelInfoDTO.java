package com.divyansh.airbnbapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDTO {
    HotelDTO hotel;
    List<RoomDTO> rooms;
    String[] photos;
}
