package com.otobus.service;

import com.otobus.entity.Terminal;
import com.otobus.repository.TerminalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TerminalService {

    private final TerminalRepository terminalRepository;

    public TerminalService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    public Terminal addTerminal(Terminal terminal) {
        return terminalRepository.save(terminal);
    }

    public List<Terminal> getAllTerminals() {
        return terminalRepository.findAll();
    }

    // 3. KURAL: Otogar silinmek istendiğinde, ona bağlı sefer varsa silinemesin.
    public void deleteTerminal(Long id) {
        // DİKKAT: Sefer (Trip) tablosunu Arda yazıyor. 
        // Onun kodları henüz main dala (ana projeye) gelmediği için şu an o kontrolü yazamayız.
        // O yüzden buraya takım çalışmasında çok kullanılan bir "TODO" (Yapılacak) notu bırakıyoruz.
        // Arda kodlarını bitirip birleştirdiğimizde, buraya "TripRepository" çağırıp kontrol ekleyeceğiz.
        
        /* TODO: Arda Trip entity'sini bitirince buraya şu mantık eklenecek:
         boolean hasTrips = tripRepository.existsByTerminalId(id);
         if(hasTrips) throw new RuntimeException("Bu otogara ait seferler var, silinemez!");
        */

        // Şimdilik sadece standart silme işlemini yapıyoruz.
        terminalRepository.deleteById(id);
    }
}