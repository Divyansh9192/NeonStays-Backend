package com.divyansh.airbnbapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class RoomPriceResponseDTO {
    private Long roomId;
    private int nights;
    private List<DailyPriceDTO> dailyPrices;
    private BigDecimal total;
}
