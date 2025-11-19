package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {

    Room findByIdAndHotelId(Long hotelId,Long roomId);
}
