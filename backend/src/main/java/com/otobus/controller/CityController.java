package com.otobus.controller;

import com.otobus.entity.City;
import com.otobus.service.CityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    public ResponseEntity<?> addCity(@RequestBody City city) {
        try {
            return ResponseEntity.ok(cityService.addCity(city));
        } catch (RuntimeException e) {
            // Eğer aynı isimde şehir varsa Service'in fırlattığı hatayı yakalayıp 400 Bad
            // Request dönüyoruz.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok("Şehir başarıyla silindi.");
    }
}