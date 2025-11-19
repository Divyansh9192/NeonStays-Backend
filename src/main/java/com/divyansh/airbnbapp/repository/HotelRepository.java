package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
