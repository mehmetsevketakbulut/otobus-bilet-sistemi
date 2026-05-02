package com.otobus.service;

import com.otobus.entity.City;
import com.otobus.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    // Repository'i Service'e enjekte ediyoruz (Constructor Injection)
    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    // 1. KURAL: Aynı isimde şehir iki defa eklenemesin!
    public City addCity(City city) {
        if (cityRepository.existsByName(city.getName())) {
            // Eğer veritabanında bu isimde bir şehir varsa, kaydetmeyi durdur ve hata
            // fırlat.
            throw new RuntimeException("Bu isimde bir şehir zaten sistemde kayıtlı: " + city.getName());
        }
        // Eğer sorun yoksa şehri veritabanına kaydet.
        return cityRepository.save(city);
    }

    // Ekstra: Şehirleri listelemek için kullanacağımız metot
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }
}