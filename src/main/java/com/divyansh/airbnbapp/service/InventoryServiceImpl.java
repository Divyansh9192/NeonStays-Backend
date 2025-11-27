package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.HotelPriceDTO;
import com.divyansh.airbnbapp.dto.HotelSearchRequestDTO;
import com.divyansh.airbnbapp.dto.InventoryDTO;
import com.divyansh.airbnbapp.dto.UpdateInventoryRequestDTO;
import com.divyansh.airbnbapp.entity.Inventory;
import com.divyansh.airbnbapp.entity.Room;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.exception.ResourceNotFoundException;
import com.divyansh.airbnbapp.repository.HotelMinRepository;
import com.divyansh.airbnbapp.repository.InventoryRepository;
import com.divyansh.airbnbapp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.divyansh.airbnbapp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    private final InventoryRepository inventoryRepository;
    private final HotelMinRepository hotelMinRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDTO> searchHotels(HotelSearchRequestDTO hotelSearchRequestDTO) {
        log.info("Searching hotel for {} city, from {} to {}", hotelSearchRequestDTO.getCity()
                , hotelSearchRequestDTO.getStartDate()
                , hotelSearchRequestDTO.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequestDTO.getPage(), hotelSearchRequestDTO.getSize());
        return hotelMinRepository.findHotelWithAvailableInventory(hotelSearchRequestDTO.getCity(),
                hotelSearchRequestDTO.getStartDate(), hotelSearchRequestDTO.getEndDate(), pageable);
    }

    @Override
    public List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all the inventory by room for room with Id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id: " + roomId));
        User user = getCurrentUser();
        if (!user.getId().equals(room.getHotel().getOwner().getId())) {
            try {
                throw new AccessDeniedException("You are not the Owner of the hotel" + room.getHotel().getId());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map((element) -> modelMapper.map(element, InventoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional

    public void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDTO) {
        log.info("Updating all the inventory for room with Id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id: " + roomId));
        User user = getCurrentUser();
        if (!user.getId().equals(room.getHotel().getOwner().getId())) {
            try {
                throw new AccessDeniedException("You are not the Owner of the hotel" + room.getHotel().getId());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,
                updateInventoryRequestDTO.getStartDate(),
                updateInventoryRequestDTO.getEndDate());

        inventoryRepository.updateInventory(roomId,
                updateInventoryRequestDTO.getStartDate(),
                updateInventoryRequestDTO.getEndDate(),
                updateInventoryRequestDTO.getSurgeFactor(),
                updateInventoryRequestDTO.getClosed());
    }
}
