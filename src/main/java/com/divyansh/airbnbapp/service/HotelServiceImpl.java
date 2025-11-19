package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.HotelDTO;
import com.divyansh.airbnbapp.dto.HotelInfoDTO;
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
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDTO createNewHotel(HotelDTO hotelDTO) {
        log.info("Creating a new hotel with name : {}",hotelDTO.getName());
        Hotel hotel = modelMapper.map(hotelDTO,Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);
        log.info("Creating a new hotel with id : {}",hotelDTO.getId());
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public HotelDTO getHotelById(Long id) {
        log.info("Getting the hotel with id: {}",id);
        Hotel hotel = hotelRepository.
                findById(id).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+id);
        }
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public List<HotelDTO> getAllHotel() {
        User user = getCurrentUser();
        log.info("Getting all the hotels for the admin user with id: "+user.getId());
        List<Hotel> hotel = hotelRepository.findByOwner(user);
        return hotel
                .parallelStream()
                .map((element) -> modelMapper.map(element, HotelDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelDTO updateHotelById(Long id, HotelDTO hotelDTO) {
        log.info("Updating the hotel with id: {}",id);
        Hotel hotel = hotelRepository.
                findById(id).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+id);
        }
        modelMapper.map(hotelDTO,hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Deleting the hotel with id: {}",id);
        Hotel hotel = hotelRepository.
                findById(id).
                orElseThrow(()->new ResourceNotFoundException("Hotel Not Found!"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+id);
        }
        for(Room room:hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotelById(Long id) {
        log.info("Activating the hotel with id: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID:"+ id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id:"+id);
        }
        hotel.setActive(true);

        //assuming only doing it once
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDTO getHotelInfoById(Long hotelId) {
        log.info("Getting Details of Hotel with id: {}",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                                     .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID:"+ hotelId));
        String[] photos = hotel.getPhotos();
        List<RoomDTO> rooms = hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDTO.class))
                .toList();
        return new HotelInfoDTO(modelMapper.map(hotel,HotelDTO.class),rooms,photos);
    }

}
