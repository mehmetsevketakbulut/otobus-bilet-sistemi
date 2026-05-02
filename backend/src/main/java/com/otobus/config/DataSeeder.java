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
        // Eğer zaten şehir varsa seeder'ı çalıştırma
        if (cityRepository.count() > 0) {
            return;
        }

        // 1. Şehirleri Ekle
        City istanbul = new City(); istanbul.setName("İstanbul");
        City ankara = new City(); ankara.setName("Ankara");
        City izmir = new City(); izmir.setName("İzmir");
        cityRepository.saveAll(List.of(istanbul, ankara, izmir));

        // 2. Terminalleri Ekle
        Terminal esenler = new Terminal(); esenler.setName("Esenler Otogarı"); esenler.setCity(istanbul);
        Terminal asti = new Terminal(); asti.setName("AŞTİ"); asti.setCity(ankara);
        Terminal izotas = new Terminal(); izotas.setName("İzotaş"); izotas.setCity(izmir);
        terminalRepository.saveAll(List.of(esenler, asti, izotas));

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

        // 4. Otobüsleri Ekle
        Bus bus1 = new Bus(); bus1.setPlate("34 IST 01"); bus1.setSeatCapacity(40); bus1.setCompany(kamilKoc);
        Bus bus2 = new Bus(); bus2.setPlate("06 ANK 02"); bus2.setSeatCapacity(40); bus2.setCompany(metro);
        Bus bus3 = new Bus(); bus3.setPlate("35 IZM 03"); bus3.setSeatCapacity(40); bus3.setCompany(pamukkale);
        busRepository.saveAll(List.of(bus1, bus2, bus3));

        // 5. Yarına Ait Seferleri Ekle
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
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

        tripRepository.saveAll(List.of(t1, t2, t3));

        System.out.println("Data Seeder: Örnek şehirler, firmalar ve seferler başarıyla eklendi!");
    }
}
