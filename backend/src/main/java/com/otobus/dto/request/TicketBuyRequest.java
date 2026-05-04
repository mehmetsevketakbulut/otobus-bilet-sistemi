package com.otobus.dto.request;

import lombok.Data;

@Data
public class TicketBuyRequest {
    private Long tripId;
    private Long fromStopId;
    private Long toStopId;
    private int koltukNo;
    private String yolcuAdSoyad;
    private String gender;  // "male" veya "female"
    private String tcNo;    // 11 haneli TC kimlik no
    private Long userId;    // giriş yapmış kullanıcının ID'si (opsiyonel)
}
