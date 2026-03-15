package com.otobus.repository;

import com.otobus.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    
    // Aynı isimde şehir iki defa eklenemesin
    // Bu sihirli metot, veritabanında o isimde şehir varsa 'true', yoksa 'false' döner.
    boolean existsByName(String name);
}