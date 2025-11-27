package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.entity.Guest;
import com.divyansh.airbnbapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}