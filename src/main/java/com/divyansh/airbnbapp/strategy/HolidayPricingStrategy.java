package com.divyansh.airbnbapp.strategy;

import com.divyansh.airbnbapp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isTodayHoliday = true;// Call a Third-party API to get Holidays

        if(isTodayHoliday){
            price = price.multiply(BigDecimal.valueOf(1.4));
        }
        return price;
    }
}
