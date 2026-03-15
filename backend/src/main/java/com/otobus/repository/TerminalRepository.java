package com.otobus.repository;

import com.otobus.entity.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    

    // JpaRepository sayesinde kaydet, sil, bul (save, delete, findById) gibi 
    // tüm temel metotlar otomatik olarak buraya miras geldi.
}