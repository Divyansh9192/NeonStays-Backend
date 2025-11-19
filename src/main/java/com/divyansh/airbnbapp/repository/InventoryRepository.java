package com.divyansh.airbnbapp.repository;

import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.Inventory;
import com.divyansh.airbnbapp.entity.Room;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    void deleteByRoom(Room room);

    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.closed=false
             AND (i.totalCount-i.bookedCount-i.reservedCount) >= :roomsCount
            GROUP BY i.hotel,i.room
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<Hotel> findHotelWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed=false
                AND (i.totalCount-i.bookedCount-i.reservedCount) >= :roomsCount""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount + :roomsCount
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed=false
                AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount""")
    void initBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") int roomsCount
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed=false
                AND i.reservedCount >= :roomsCount""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") int roomsCount);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount - :roomsCount,
                   i.bookedCount = i.bookedCount + :roomsCount
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed=false
                AND i.reservedCount >= :roomsCount""")
    void confirmBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") int roomsCount
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Inventory i
            SET  i.bookedCount = i.bookedCount - :roomsCount
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate
                AND i.closed=false
                AND (i.totalCount-i.bookedCount) >= :roomsCount""")
    void cancelBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") int roomsCount
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel
                                              ,LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomOrderByDate(Room room);


    @Modifying
    @Transactional
    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getInventoryAndLockBeforeUpdate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Inventory i
            SET  i.surgeFactor = :surgeFactor,
                 i.closed = :closed
            WHERE i.room.id = :roomId
                AND i.date BETWEEN :startDate AND :endDate""")
    void updateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("surgeFactor") BigDecimal surgeFactor,
            @Param("closed") Boolean closed
    );
}


