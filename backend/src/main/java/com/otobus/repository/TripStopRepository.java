package com.otobus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otobus.entity.TripStop;

@Repository
public interface TripStopRepository extends JpaRepository<TripStop, Long> {
}
