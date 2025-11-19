package com.divyansh.airbnbapp.strategy;

import com.divyansh.airbnbapp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
