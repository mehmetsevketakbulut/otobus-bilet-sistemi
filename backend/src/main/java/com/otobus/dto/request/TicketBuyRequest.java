package com.otobus.dto.request;

import lombok.Data;

@Data
public class TicketBuyRequest {
    private Long tripId;
    private Long fromStopId;
    private Long toStopId;
    private int koltukNo;
    private String yolcuAdSoyad;
}
