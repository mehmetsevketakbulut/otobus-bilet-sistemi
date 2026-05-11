package com.otobus.repository;

import com.otobus.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByPlate(String plate);
    List<Bus> findByCompanyId(Long companyId);
}