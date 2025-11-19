package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}