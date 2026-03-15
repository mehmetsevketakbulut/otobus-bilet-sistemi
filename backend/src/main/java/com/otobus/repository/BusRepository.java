package com.otobus.repository;

import com.otobus.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    
    // Otobüs eklerken "Bu plaka zaten sistemde var mı?" kontrolü.
    // Veritabanına plakayı sorup true/false cevabı alacağız.
    boolean existsByPlate(String plate);
}