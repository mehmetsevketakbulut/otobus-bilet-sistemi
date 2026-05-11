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
                    .role(Role.ADMIN).emailVerified(true).build();
            userRepository.save(admin);

            User kamilUser = User.builder().fullName("Kamil Koç").email("kamilkoc@otobilet.com")
                    .phoneNumber("05551111111").password(passwordEncoder.encode("firma123"))
                    .role(Role.COMPANY).emailVerified(true).build();
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

        // 5. Bugün ve Yarına Ait Seferleri Ekle
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Eğer zaten seferler eklendiyse tekrar ekleme
        if (tripRepository.count() == 0 || !hasTrabzon) {

            // ══════════════════════════════════
            // BUGÜN - Seferler
            // ══════════════════════════════════

            // İstanbul → Ankara (Kamil Koç, Sabah)
            Trip tb1 = createTrip(esenler, asti, bus1, LocalDateTime.of(today, LocalTime.of(8, 0)), 450.0, true);
            tb1.setStops(List.of(
                createStop(tb1, esenler, 1, tb1.getKalkisSaati(), 0),
                createStop(tb1, asti, 2, tb1.getKalkisSaati().plusHours(5), 450)
            ));

            // İstanbul → Ankara (Metro, Öğlen)
            Trip tb2 = createTrip(esenler, asti, bus2, LocalDateTime.of(today, LocalTime.of(13, 0)), 420.0, true);
            tb2.setStops(List.of(
                createStop(tb2, esenler, 1, tb2.getKalkisSaati(), 0),
                createStop(tb2, asti, 2, tb2.getKalkisSaati().plusHours(5), 420)
            ));

            // İstanbul → Ankara (Pamukkale, Akşam)
            Trip tb3 = createTrip(esenler, asti, bus3, LocalDateTime.of(today, LocalTime.of(20, 0)), 400.0, true);
            tb3.setStops(List.of(
                createStop(tb3, esenler, 1, tb3.getKalkisSaati(), 0),
                createStop(tb3, asti, 2, tb3.getKalkisSaati().plusHours(5), 400)
            ));

            // Ankara → İstanbul (Metro, Sabah)
            Trip tb4 = createTrip(asti, esenler, bus2, LocalDateTime.of(today, LocalTime.of(9, 30)), 430.0, true);
            tb4.setStops(List.of(
                createStop(tb4, asti, 1, tb4.getKalkisSaati(), 0),
                createStop(tb4, esenler, 2, tb4.getKalkisSaati().plusHours(5), 430)
            ));

            // İstanbul → İzmir (Pamukkale, Sabah)
            Trip tb5 = createTrip(esenler, izotas, bus3, LocalDateTime.of(today, LocalTime.of(7, 0)), 600.0, true);
            tb5.setStops(List.of(
                createStop(tb5, esenler, 1, tb5.getKalkisSaati(), 0),
                createStop(tb5, izotas, 2, tb5.getKalkisSaati().plusHours(7), 600)
            ));

            // İstanbul → İzmir (Kamil Koç, Akşam)
            Trip tb6 = createTrip(esenler, izotas, bus1, LocalDateTime.of(today, LocalTime.of(18, 0)), 580.0, true);
            tb6.setStops(List.of(
                createStop(tb6, esenler, 1, tb6.getKalkisSaati(), 0),
                createStop(tb6, izotas, 2, tb6.getKalkisSaati().plusHours(7), 580)
            ));

            // İzmir → İstanbul (Metro, Öğlen)
            Trip tb7 = createTrip(izotas, esenler, bus2, LocalDateTime.of(today, LocalTime.of(11, 0)), 590.0, true);
            tb7.setStops(List.of(
                createStop(tb7, izotas, 1, tb7.getKalkisSaati(), 0),
                createStop(tb7, esenler, 2, tb7.getKalkisSaati().plusHours(7), 590)
            ));

            // Ankara → İzmir (Pamukkale, Sabah)
            Trip tb8 = createTrip(asti, izotas, bus3, LocalDateTime.of(today, LocalTime.of(10, 0)), 550.0, true);
            tb8.setStops(List.of(
                createStop(tb8, asti, 1, tb8.getKalkisSaati(), 0),
                createStop(tb8, izotas, 2, tb8.getKalkisSaati().plusHours(6), 550)
            ));

            // İstanbul → Antalya (Kamil Koç, Çok Duraklı, Bugün)
            Trip tb9 = createTrip(esenler, antalyaOto, bus1, LocalDateTime.of(today, LocalTime.of(6, 0)), 850.0, true);
            tb9.setStops(List.of(
                createStop(tb9, esenler, 1, tb9.getKalkisSaati(), 0),
                createStop(tb9, asti, 2, tb9.getKalkisSaati().plusHours(5), 450),
                createStop(tb9, konyaOto, 3, tb9.getKalkisSaati().plusHours(8), 600),
                createStop(tb9, antalyaOto, 4, tb9.getKalkisSaati().plusHours(12), 850)
            ));

            // Trabzon → İstanbul (Metro, Bugün)
            Trip tb10 = createTrip(trabzonOto, esenler, bus2, LocalDateTime.of(today, LocalTime.of(15, 0)), 750.0, true);
            tb10.setStops(List.of(
                createStop(tb10, trabzonOto, 1, tb10.getKalkisSaati(), 0),
                createStop(tb10, samsunOto, 2, tb10.getKalkisSaati().plusHours(4), 300),
                createStop(tb10, esenler, 3, tb10.getKalkisSaati().plusHours(12), 750)
            ));

            // ══════════════════════════════════
            // YARIN - Seferler
            // ══════════════════════════════════

            // İstanbul → Ankara (Kamil Koç, Sabah)
            Trip ty1 = createTrip(esenler, asti, bus1, LocalDateTime.of(tomorrow, LocalTime.of(7, 0)), 500.0, true);
            ty1.setStops(List.of(
                createStop(ty1, esenler, 1, ty1.getKalkisSaati(), 0),
                createStop(ty1, asti, 2, ty1.getKalkisSaati().plusHours(5), 500)
            ));

            // İstanbul → Ankara (Metro, Öğlen)
            Trip ty2 = createTrip(esenler, asti, bus2, LocalDateTime.of(tomorrow, LocalTime.of(12, 0)), 470.0, true);
            ty2.setStops(List.of(
                createStop(ty2, esenler, 1, ty2.getKalkisSaati(), 0),
                createStop(ty2, asti, 2, ty2.getKalkisSaati().plusHours(5), 470)
            ));

            // İstanbul → Ankara (Pamukkale, Gece)
            Trip ty3 = createTrip(esenler, asti, bus3, LocalDateTime.of(tomorrow, LocalTime.of(22, 0)), 380.0, true);
            ty3.setStops(List.of(
                createStop(ty3, esenler, 1, ty3.getKalkisSaati(), 0),
                createStop(ty3, asti, 2, ty3.getKalkisSaati().plusHours(5), 380)
            ));

            // Ankara → İstanbul (Kamil Koç, Sabah)
            Trip ty4 = createTrip(asti, esenler, bus1, LocalDateTime.of(tomorrow, LocalTime.of(8, 30)), 480.0, true);
            ty4.setStops(List.of(
                createStop(ty4, asti, 1, ty4.getKalkisSaati(), 0),
                createStop(ty4, esenler, 2, ty4.getKalkisSaati().plusHours(5), 480)
            ));

            // Ankara → İstanbul (Metro, Akşam)
            Trip ty5 = createTrip(asti, esenler, bus2, LocalDateTime.of(tomorrow, LocalTime.of(17, 0)), 460.0, true);
            ty5.setStops(List.of(
                createStop(ty5, asti, 1, ty5.getKalkisSaati(), 0),
                createStop(ty5, esenler, 2, ty5.getKalkisSaati().plusHours(5), 460)
            ));

            // İstanbul → İzmir (Pamukkale, Sabah)
            Trip ty6 = createTrip(esenler, izotas, bus3, LocalDateTime.of(tomorrow, LocalTime.of(9, 0)), 650.0, true);
            ty6.setStops(List.of(
                createStop(ty6, esenler, 1, ty6.getKalkisSaati(), 0),
                createStop(ty6, izotas, 2, ty6.getKalkisSaati().plusHours(7), 650)
            ));

            // İstanbul → İzmir (Kamil Koç, Akşam)
            Trip ty7 = createTrip(esenler, izotas, bus1, LocalDateTime.of(tomorrow, LocalTime.of(19, 0)), 620.0, true);
            ty7.setStops(List.of(
                createStop(ty7, esenler, 1, ty7.getKalkisSaati(), 0),
                createStop(ty7, izotas, 2, ty7.getKalkisSaati().plusHours(7), 620)
            ));

            // İzmir → Ankara (Metro, Sabah)
            Trip ty8 = createTrip(izotas, asti, bus2, LocalDateTime.of(tomorrow, LocalTime.of(6, 30)), 520.0, true);
            ty8.setStops(List.of(
                createStop(ty8, izotas, 1, ty8.getKalkisSaati(), 0),
                createStop(ty8, asti, 2, ty8.getKalkisSaati().plusHours(6), 520)
            ));

            // Trabzon → Antalya Çok Duraklı (Kamil Koç, Yarın)
            Trip ty9 = createTrip(trabzonOto, antalyaOto, bus1, LocalDateTime.of(tomorrow, LocalTime.of(8, 0)), 1200.0, true);
            ty9.setStops(List.of(
                createStop(ty9, trabzonOto, 1, ty9.getKalkisSaati(), 0),
                createStop(ty9, samsunOto, 2, ty9.getKalkisSaati().plusHours(5), 400),
                createStop(ty9, asti, 3, ty9.getKalkisSaati().plusHours(11), 700),
                createStop(ty9, konyaOto, 4, ty9.getKalkisSaati().plusHours(14), 900),
                createStop(ty9, antalyaOto, 5, ty9.getKalkisSaati().plusHours(18), 1200)
            ));

            // Antalya → İstanbul (Pamukkale, Yarın)
            Trip ty10 = createTrip(antalyaOto, esenler, bus3, LocalDateTime.of(tomorrow, LocalTime.of(10, 0)), 800.0, true);
            ty10.setStops(List.of(
                createStop(ty10, antalyaOto, 1, ty10.getKalkisSaati(), 0),
                createStop(ty10, konyaOto, 2, ty10.getKalkisSaati().plusHours(4), 350),
                createStop(ty10, esenler, 3, ty10.getKalkisSaati().plusHours(10), 800)
            ));

            // Samsun → Ankara (Metro, Yarın)
            Trip ty11 = createTrip(samsunOto, asti, bus2, LocalDateTime.of(tomorrow, LocalTime.of(14, 0)), 380.0, true);
            ty11.setStops(List.of(
                createStop(ty11, samsunOto, 1, ty11.getKalkisSaati(), 0),
                createStop(ty11, asti, 2, ty11.getKalkisSaati().plusHours(6), 380)
            ));

            // Konya → İstanbul (Kamil Koç, Yarın)
            Trip ty12 = createTrip(konyaOto, esenler, bus1, LocalDateTime.of(tomorrow, LocalTime.of(11, 0)), 420.0, true);
            ty12.setStops(List.of(
                createStop(ty12, konyaOto, 1, ty12.getKalkisSaati(), 0),
                createStop(ty12, esenler, 2, ty12.getKalkisSaati().plusHours(6), 420)
            ));

            tripRepository.saveAll(List.of(
                tb1, tb2, tb3, tb4, tb5, tb6, tb7, tb8, tb9, tb10,
                ty1, ty2, ty3, ty4, ty5, ty6, ty7, ty8, ty9, ty10, ty11, ty12
            ));
        }

        System.out.println("Data Seeder: Örnek şehirler, firmalar ve " + tripRepository.count() + " sefer başarıyla eklendi!");
    }

    // Helper: Trip oluştur
    private Trip createTrip(Terminal kalkis, Terminal varis, Bus otobus, LocalDateTime kalkisSaati, double fiyat, boolean approved) {
        Trip trip = new Trip();
        trip.setKalkisTerminali(kalkis);
        trip.setVarisTerminali(varis);
        trip.setOtobus(otobus);
        trip.setKalkisSaati(kalkisSaati);
        trip.setFiyat(fiyat);
        trip.setApproved(approved);
        return trip;
    }

    // Helper: TripStop oluştur
    private TripStop createStop(Trip trip, Terminal terminal, int order, LocalDateTime departureTime, double priceFromStart) {
        TripStop stop = new TripStop();
        stop.setTrip(trip);
        stop.setTerminal(terminal);
        stop.setStopOrder(order);
        stop.setDepartureTime(departureTime);
        stop.setPriceFromStart(priceFromStart);
        return stop;
    }
}
