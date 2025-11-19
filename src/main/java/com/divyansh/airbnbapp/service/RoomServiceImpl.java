package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.RoomDTO;
import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.Room;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.exception.ResourceNotFoundException;
import com.divyansh.airbnbapp.exception.UnauthorizedException;
import com.divyansh.airbnbapp.repository.HotelRepository;
import com.divyansh.airbnbapp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.divyansh.airbnbapp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDTO createNewRoom(Long hotelId,RoomDTO roomDTO) {
        log.info("Creating a new room in hotel with id: {} ",hotelId);
        Hotel hotel = hotelRepository.
                findById(hotelId).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+hotelId);
        }

        Room room = modelMapper.map(roomDTO,Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room,RoomDTO.class);
    }

    @Override
    public List<RoomDTO> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with id: {} ",hotelId);
        Hotel hotel = hotelRepository.
                findById(hotelId).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+hotelId);
        }
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDTO.class)).collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Long hotelId,Long roomId) {
        log.info("Getting the room with id: {} ", roomId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not Found with id:" + hotelId));

        Room room = roomRepository.findByIdAndHotelId(roomId,hotelId);
        if(room ==null){
            throw new ResourceNotFoundException("Room not found with id: "+roomId);
        }
        return modelMapper.map(room,RoomDTO.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with id: {} ", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not Found with id:" + roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getId().equals(room.getHotel().getOwner().getId())){
            throw new UnauthorizedException("This user does not own this room with id:"+roomId);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }

    @Override
    @Transactional
    public RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDTO) {
        log.info("Updating the room with id: {}",roomId);
        Hotel hotel = hotelRepository.
                findById(hotelId).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
                throw new UnauthorizedException("This user does not own this hotel with id:"+hotelId);
        }
        Room room = roomRepository.findById(roomId)
                        .orElseThrow(()-> new ResourceNotFoundException("Room not found with Id"+roomId));
        modelMapper.map(roomDTO,room);
        room.setId(roomId);

        room = roomRepository.save(room);
        return modelMapper.map(room,RoomDTO.class);
    }
}
