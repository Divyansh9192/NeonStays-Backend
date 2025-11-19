package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.HotelDTO;
import com.divyansh.airbnbapp.dto.HotelInfoDTO;
import com.divyansh.airbnbapp.entity.Hotel;

import java.util.List;

public interface HotelService {
    HotelDTO createNewHotel(HotelDTO hotelDTO);

    HotelDTO getHotelById(Long id);

    List<HotelDTO> getAllHotel();

    HotelDTO updateHotelById(Long id, HotelDTO hotelDTO);

    void deleteHotelById(Long id);

    void activateHotelById(Long id);

    HotelInfoDTO getHotelInfoById(Long hotelId);
}
