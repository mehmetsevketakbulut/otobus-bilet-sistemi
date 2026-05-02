package com.otobus.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TripSearchResponse {
    private Long tripId;
    private String companyName;
    private String departureTerminal;
    private String arrivalTerminal;
    private LocalDateTime departureTime;
    private double price;
    private int seatCapacity;
    private Long fromStopId;
    private Long toStopId;
}
