package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.dto.HotelPriceDTO;
import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelMinRepository extends JpaRepository<HotelMinPrice,Long> {

    @Query("""
            SELECT DISTINCT new com.divyansh.airbnbapp.dto.HotelPriceDTO(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.hotel.active=true
            GROUP BY i.hotel
            """)
    Page<HotelPriceDTO> findHotelWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
