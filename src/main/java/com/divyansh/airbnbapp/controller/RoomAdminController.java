package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.RoomDTO;
import com.divyansh.airbnbapp.entity.Room;
import com.divyansh.airbnbapp.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/hotels/{hotelId}/rooms")
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDTO> createNewRoom(@PathVariable Long hotelId,
                                                 @RequestBody RoomDTO roomDTO){
        RoomDTO roomDTO1 = roomService.createNewRoom(hotelId,roomDTO);
        return new ResponseEntity<>(roomDTO1, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRoomsInHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long hotelId,@PathVariable Long roomId){

        return ResponseEntity.ok(roomService.getRoomById(hotelId,roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDTO> updateRoomById(@PathVariable Long hotelId,
                                                  @PathVariable Long roomId,
                                                  @RequestBody RoomDTO roomDTO){
        return ResponseEntity.ok(roomService.updateRoomById(hotelId,roomId,roomDTO));

    }
}
