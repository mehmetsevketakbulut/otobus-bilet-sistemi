package com.otobus.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatStatusResponse {
    private int seatNo;
    private String status;  // "available" veya "occupied"
    private String gender;  // "male", "female" veya null
}
