package com.divyansh.airbnbapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyPriceDTO {
    private LocalDate date;
    private BigDecimal price;
}
