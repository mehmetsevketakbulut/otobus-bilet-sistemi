package com.otobus.controller;

import com.otobus.entity.Bus;
import com.otobus.service.BusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PostMapping
    public ResponseEntity<?> addBus(@RequestBody Bus bus) {
        try {
            return ResponseEntity.ok(busService.addBus(bus));
        } catch (RuntimeException e) {
            // Eğer aynı plaka varsa Service'in fırlattığı hatayı yakalıyoruz.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.ok("Otobüs başarıyla silindi.");
    }
}