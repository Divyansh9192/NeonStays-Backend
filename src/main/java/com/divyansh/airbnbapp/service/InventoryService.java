package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.HotelPriceDTO;
import com.divyansh.airbnbapp.dto.HotelSearchRequestDTO;
import com.divyansh.airbnbapp.dto.InventoryDTO;
import com.divyansh.airbnbapp.dto.UpdateInventoryRequestDTO;
import com.divyansh.airbnbapp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

//    void deleteFutureInventories(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDTO> searchHotels(HotelSearchRequestDTO hotelSearchRequestDTO);

    List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO);
}
