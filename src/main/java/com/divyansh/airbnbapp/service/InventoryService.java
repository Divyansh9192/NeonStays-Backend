package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.*;
import com.divyansh.airbnbapp.entity.Room;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

//    void deleteFutureInventories(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDTO> searchHotels(HotelSearchRequestDTO hotelSearchRequestDTO);

    List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO);

    RoomPriceResponseDTO getRoomDynamicPrice(Long roomId, LocalDate start, LocalDate end);
}
