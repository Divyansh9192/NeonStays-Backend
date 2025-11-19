package com.divyansh.airbnbapp.service;


import com.divyansh.airbnbapp.dto.RoomDTO;

import java.util.List;

public interface RoomService {

    RoomDTO createNewRoom(Long hotelId,RoomDTO roomDTO);

    List<RoomDTO> getAllRoomsInHotel(Long hotelId);

    RoomDTO getRoomById(Long hotelId,Long roomId);

    void deleteRoomById(Long roomId);

    RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDTO);
}
