package com.otobus.config;

import com.otobus.entity.*;
import com.otobus.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final TerminalRepository terminalRepository;
    private final CompanyRepository companyRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataSeeder(CityRepository cityRepository, TerminalRepository terminalRepository,
                      CompanyRepository companyRepository, BusRepository busRepository,
                      TripRepository tripRepository, UserRepository userRepository,
                      org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.cityRepository = cityRepository;
        this.terminalRepository = terminalRepository;
        this.companyRepository = companyRepository;
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Sadece yeni verileri eklemek için check edelim
        boolean hasTrabzon = cityRepository.findAll().stream().anyMatch(c -> c.getName().equals("Trabzon"));
        if (hasTrabzon) return; // Zaten çalışmış


        City istanbul = cityRepository.findAll().stream().filter(c -> c.getName().equals("İstanbul")).findFirst().orElseGet(() -> { City c = new City(); c.setName("İstanbul"); return cityRepository.save(c); });
        City ankara = cityRepository.findAll().stream().filter(c -> c.getName().equals("Ankara")).findFirst().orElseGet(() -> { City c = new City(); c.setName("Ankara"); return cityRepository.save(c); });
        City izmir = cityRepository.findAll().stream().filter(c -> c.getName().equals("İzmir")).findFirst().orElseGet(() -> { City c = new City(); c.setName("İzmir"); return cityRepository.save(c); });
        City trabzon = cityRepository.findAll().stream().filter(c -> c.getName().equals("Trabzon")).findFirst().orElseGet(() -> { City c = new City(); c.setName("Trabzon"); return cityRepository.save(c); });
        City samsun = cityRepository.findAll().stream().filter(c -> c.getName().equals("Samsun")).findFirst().orElseGet(() -> { City c = new City(); c.setName("Samsun"); return cityRepository.save(c); });
        City konya = cityRepository.findAll().stream().filter(c -> c.getName().equals("Konya")).findFirst().orElseGet(() -> { City c = new City(); c.setName("Konya"); return cityRepository.save(c); });
        City antalya = cityRepository.findAll().stream().filter(c -> c.getName().equals("Antalya")).findFirst().orElseGet(() -> { City c = new City(); c.setName("Antalya"); return cityRepository.save(c); });

        // 2. Terminalleri Ekle
        Terminal esenler = terminalRepository.findAll().stream().filter(t -> t.getName().equals("Esenler Otogarı")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("Esenler Otogarı"); t.setCity(istanbul); return terminalRepository.save(t); });
        Terminal asti = terminalRepository.findAll().stream().filter(t -> t.getName().equals("AŞTİ")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("AŞTİ"); t.setCity(ankara); return terminalRepository.save(t); });
        Terminal izotas = terminalRepository.findAll().stream().filter(t -> t.getName().equals("İzotaş")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("İzotaş"); t.setCity(izmir); return terminalRepository.save(t); });
        Terminal trabzonOto = terminalRepository.findAll().stream().filter(t -> t.getName().equals("Trabzon Otogarı")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("Trabzon Otogarı"); t.setCity(trabzon); return terminalRepository.save(t); });
        Terminal samsunOto = terminalRepository.findAll().stream().filter(t -> t.getName().equals("Samsun Otogarı")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("Samsun Otogarı"); t.setCity(samsun); return terminalRepository.save(t); });
        Terminal konyaOto = terminalRepository.findAll().stream().filter(t -> t.getName().equals("Konya Otogarı")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("Konya Otogarı"); t.setCity(konya); return terminalRepository.save(t); });
        Terminal antalyaOto = terminalRepository.findAll().stream().filter(t -> t.getName().equals("Antalya Otogarı")).findFirst().orElseGet(() -> { Terminal t = new Terminal(); t.setName("Antalya Otogarı"); t.setCity(antalya); return terminalRepository.save(t); });

        if (userRepository.count() == 0) {
            User admin = User.builder().fullName("Admin").email("admin@otobilet.com")
                    .phoneNumber("05550000000").password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN).build();
            userRepository.save(admin);

            User kamilUser = User.builder().fullName("Kamil Koç").email("kamilkoc@otobilet.com")
                    .phoneNumber("05551111111").password(passwordEncoder.encode("firma123"))
                    .role(Role.COMPANY).build();
            userRepository.save(kamilUser);
            
            Company kamilKoc = new Company(); kamilKoc.setName("Kamil Koç"); kamilKoc.setOwner(kamilUser);
            Company metro = new Company(); metro.setName("Metro Turizm");
            Company pamukkale = new Company(); pamukkale.setName("Pamukkale");
            companyRepository.saveAll(List.of(kamilKoc, metro, pamukkale));
        }

        Company kamilKoc = companyRepository.findAll().stream().filter(c -> c.getName().equals("Kamil Koç")).findFirst().orElseThrow();
        Company metro = companyRepository.findAll().stream().filter(c -> c.getName().equals("Metro Turizm")).findFirst().orElseThrow();
        Company pamukkale = companyRepository.findAll().stream().filter(c -> c.getName().equals("Pamukkale")).findFirst().orElseThrow();

        // 4. Otobüsleri Ekle (Zaten ekliyse alma)
        Bus bus1 = busRepository.findAll().stream().filter(b -> b.getPlate().equals("34 IST 01")).findFirst().orElseGet(() -> {
            Bus b = new Bus(); b.setPlate("34 IST 01"); b.setSeatCapacity(40); b.setCompany(kamilKoc); return busRepository.save(b);
        });
        Bus bus2 = busRepository.findAll().stream().filter(b -> b.getPlate().equals("06 ANK 02")).findFirst().orElseGet(() -> {
            Bus b = new Bus(); b.setPlate("06 ANK 02"); b.setSeatCapacity(40); b.setCompany(metro); return busRepository.save(b);
        });
        Bus bus3 = busRepository.findAll().stream().filter(b -> b.getPlate().equals("35 IZM 03")).findFirst().orElseGet(() -> {
            Bus b = new Bus(); b.setPlate("35 IZM 03"); b.setSeatCapacity(40); b.setCompany(pamukkale); return busRepository.save(b);
        });

        // 5. Yarına Ait Seferleri Ekle
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Eğer zaten seferler eklendiyse tekrar ekleme
        if (tripRepository.count() == 0 || !hasTrabzon) {
            Trip t1 = new Trip();
            t1.setKalkisTerminali(esenler);
            t1.setVarisTerminali(asti);
            t1.setOtobus(bus1);
            t1.setKalkisSaati(LocalDateTime.of(tomorrow, LocalTime.of(10, 0)));
            t1.setFiyat(500.0);
            t1.setApproved(true);
            TripStop t1s1 = new TripStop(); t1s1.setTrip(t1); t1s1.setTerminal(esenler); t1s1.setStopOrder(1); t1s1.setDepartureTime(t1.getKalkisSaati()); t1s1.setPriceFromStart(0);
            TripStop t1s2 = new TripStop(); t1s2.setTrip(t1); t1s2.setTerminal(asti); t1s2.setStopOrder(2); t1s2.setDepartureTime(t1.getKalkisSaati().plusHours(5)); t1s2.setPriceFromStart(500);
            t1.setStops(List.of(t1s1, t1s2));

            Trip t2 = new Trip();
            t2.setKalkisTerminali(esenler);
            t2.setVarisTerminali(izotas);
            t2.setOtobus(bus3);
            t2.setKalkisSaati(LocalDateTime.of(tomorrow, LocalTime.of(12, 30)));
            t2.setFiyat(650.0);
            t2.setApproved(true);
            TripStop t2s1 = new TripStop(); t2s1.setTrip(t2); t2s1.setTerminal(esenler); t2s1.setStopOrder(1); t2s1.setDepartureTime(t2.getKalkisSaati()); t2s1.setPriceFromStart(0);
            TripStop t2s2 = new TripStop(); t2s2.setTrip(t2); t2s2.setTerminal(izotas); t2s2.setStopOrder(2); t2s2.setDepartureTime(t2.getKalkisSaati().plusHours(7)); t2s2.setPriceFromStart(650);
            t2.setStops(List.of(t2s1, t2s2));

            Trip t3 = new Trip();
            t3.setKalkisTerminali(asti);
            t3.setVarisTerminali(esenler);
            t3.setOtobus(bus2);
            t3.setKalkisSaati(LocalDateTime.of(tomorrow, LocalTime.of(14, 0)));
            t3.setFiyat(480.0);
            t3.setApproved(true);
            TripStop t3s1 = new TripStop(); t3s1.setTrip(t3); t3s1.setTerminal(asti); t3s1.setStopOrder(1); t3s1.setDepartureTime(t3.getKalkisSaati()); t3s1.setPriceFromStart(0);
            TripStop t3s2 = new TripStop(); t3s2.setTrip(t3); t3s2.setTerminal(esenler); t3s2.setStopOrder(2); t3s2.setDepartureTime(t3.getKalkisSaati().plusHours(5)); t3s2.setPriceFromStart(480);
            t3.setStops(List.of(t3s1, t3s2));

            // YENİ: Trabzon -> Antalya Çok Duraklı Sefer (Senaryo Testi İçin)
            Trip t4 = new Trip();
            t4.setKalkisTerminali(trabzonOto);
            t4.setVarisTerminali(antalyaOto);
            t4.setOtobus(bus1); // Kamil Koç Otobüsü
            t4.setKalkisSaati(LocalDateTime.of(tomorrow, LocalTime.of(8, 0)));
            t4.setFiyat(1200.0);
            t4.setApproved(true);
            
            TripStop t4s1 = new TripStop(); t4s1.setTrip(t4); t4s1.setTerminal(trabzonOto); t4s1.setStopOrder(1); t4s1.setDepartureTime(t4.getKalkisSaati()); t4s1.setPriceFromStart(0);
            TripStop t4s2 = new TripStop(); t4s2.setTrip(t4); t4s2.setTerminal(samsunOto); t4s2.setStopOrder(2); t4s2.setDepartureTime(t4.getKalkisSaati().plusHours(5)); t4s2.setPriceFromStart(400);
            TripStop t4s3 = new TripStop(); t4s3.setTrip(t4); t4s3.setTerminal(asti); t4s3.setStopOrder(3); t4s3.setDepartureTime(t4.getKalkisSaati().plusHours(11)); t4s3.setPriceFromStart(700);
            TripStop t4s4 = new TripStop(); t4s4.setTrip(t4); t4s4.setTerminal(konyaOto); t4s4.setStopOrder(4); t4s4.setDepartureTime(t4.getKalkisSaati().plusHours(14)); t4s4.setPriceFromStart(900);
            TripStop t4s5 = new TripStop(); t4s5.setTrip(t4); t4s5.setTerminal(antalyaOto); t4s5.setStopOrder(5); t4s5.setDepartureTime(t4.getKalkisSaati().plusHours(18)); t4s5.setPriceFromStart(1200);
            t4.setStops(List.of(t4s1, t4s2, t4s3, t4s4, t4s5));

            tripRepository.saveAll(List.of(t1, t2, t3, t4));
        }

        System.out.println("Data Seeder: Örnek şehirler, firmalar ve seferler başarıyla eklendi!");
    }
}
