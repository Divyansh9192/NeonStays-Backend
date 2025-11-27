package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.HotelDTO;
import com.divyansh.airbnbapp.dto.HotelInfoDTO;
import com.divyansh.airbnbapp.dto.HotelPriceDTO;
import com.divyansh.airbnbapp.dto.HotelSearchRequestDTO;
import com.divyansh.airbnbapp.service.HotelService;
import com.divyansh.airbnbapp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/hotels/search")
@RequiredArgsConstructor
@Slf4j
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<Page<HotelPriceDTO>> searchHotels(
            @RequestBody HotelSearchRequestDTO hotelSearchRequestDTO
    ) {
        ;
        var page = inventoryService.searchHotels(hotelSearchRequestDTO);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDTO> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
