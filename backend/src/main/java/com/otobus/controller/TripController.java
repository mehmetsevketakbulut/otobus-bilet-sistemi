package com.otobus.controller;

import com.otobus.dto.response.TripSearchResponse;
import com.otobus.entity.Trip;
import com.otobus.repository.TripRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;
    private final com.otobus.service.TripService tripService;

    public TripController(TripRepository tripRepository, com.otobus.service.TripService tripService) {
        this.tripRepository = tripRepository;
        this.tripService = tripService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripSearchResponse>> searchTrips(
            @RequestParam String fromCity,
            @RequestParam String toCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Object[]> results = tripRepository.findTripsBySearchCriteria(fromCity, toCity, startOfDay, endOfDay);

        List<TripSearchResponse> response = results.stream()
                .map(obj -> {
                    Trip trip = (Trip) obj[0];
                    com.otobus.entity.TripStop s1 = (com.otobus.entity.TripStop) obj[1];
                    com.otobus.entity.TripStop s2 = (com.otobus.entity.TripStop) obj[2];

                    return TripSearchResponse.builder()
                            .tripId(trip.getId())
                            .companyName(trip.getOtobus().getCompany().getName())
                            .departureTerminal(s1.getTerminal().getName())
                            .arrivalTerminal(s2.getTerminal().getName())
                            .departureTime(s1.getDepartureTime())
                            .price(s2.getPriceFromStart() - s1.getPriceFromStart())
                            .seatCapacity(trip.getOtobus().getSeatCapacity())
                            .fromStopId(s1.getId())
                            .toStopId(s2.getId())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<List<Trip>> getPendingTrips() {
        return ResponseEntity.ok(tripService.getPendingTrips());
    }

    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<Trip> approveTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.approveTrip(id));
    }

    @PostMapping("/company")
    public ResponseEntity<Trip> addTrip(@RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.addTrip(trip));
    }
}