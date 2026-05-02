package com.otobus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.otobus.entity.Trip;
import com.otobus.entity.TripStop;
import com.otobus.repository.TripRepository;

@Service
public class TripService {
    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip addTrip(Trip trip) {
        trip.setApproved(false);
        if (trip.getStops() != null) {
            for (TripStop stop : trip.getStops()) {
                stop.setTrip(trip);
            }
        }
        return tripRepository.save(trip);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getPendingTrips() {
        return tripRepository.findAll().stream()
                .filter(t -> !t.isApproved())
                .toList();
    }

    public Trip approveTrip(Long id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new RuntimeException("Sefer bulunamadı"));
        trip.setApproved(true);
        return tripRepository.save(trip);
    }
}