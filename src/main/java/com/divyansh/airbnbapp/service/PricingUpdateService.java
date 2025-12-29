package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.entity.Hotel;
import com.divyansh.airbnbapp.entity.HotelMinPrice;
import com.divyansh.airbnbapp.entity.Inventory;
import com.divyansh.airbnbapp.repository.HotelMinRepository;
import com.divyansh.airbnbapp.repository.HotelRepository;
import com.divyansh.airbnbapp.repository.InventoryRepository;
import com.divyansh.airbnbapp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinRepository hotelMinRepository;
    private final PricingService pricingService;

//    @Scheduled(cron = "*/60 * * * * *")
    @Scheduled(cron = "0 0 * * * *")
    public void updatePrice(){
        int page=0;
        int batchsize=100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchsize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }
    private void updateHotelPrices(Hotel hotel){
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrice = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelMinPrices = new ArrayList<>();
        dailyMinPrice.forEach((date, price) -> {
            HotelMinPrice hotelMinPrice = hotelMinRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, price));
            hotelMinPrice.setDate(date);
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });
        hotelMinRepository.saveAll(hotelMinPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
         inventoryList.forEach(inventory -> {
             BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
             inventory.setPrice(dynamicPrice);
         });
         inventoryRepository.saveAll(inventoryList);
    }

}
