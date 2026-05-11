package com.otobus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusResponse {
    private int seatNo;
    private String status;  // "available" veya "occupied"
    private String gender;  // "male", "female" veya null
}
