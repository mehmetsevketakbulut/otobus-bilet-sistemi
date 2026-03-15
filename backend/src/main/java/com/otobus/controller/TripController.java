package com.otobus.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otobus.entity.Trip;
import com.otobus.repository.TripRepository;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;

    public TripController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @GetMapping("/all") 
    public List<Trip> getAll() {
        return tripRepository.findAll();
    }
    
    @PostMapping("/add")
    public Trip createTrip(@RequestBody Trip trip) {
        return tripRepository.save(trip);
    }
}