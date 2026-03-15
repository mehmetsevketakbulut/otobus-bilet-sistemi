package com.otobus.service;

import com.otobus.entity.Bus;
import com.otobus.repository.BusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusService {

    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    // 2. KURAL: Otobüs eklerken plaka kontrolü yapılsın!
    public Bus addBus(Bus bus) {
        if (busRepository.existsByPlate(bus.getPlate())) {
            // Veritabanına plakayı soruyoruz. Varsa hata fırlatıp işlemi kesiyoruz.
            throw new RuntimeException("Bu plaka zaten sistemde kayıtlı: " + bus.getPlate());
        }
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
}