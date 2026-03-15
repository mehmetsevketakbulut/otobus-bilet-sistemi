    package com.otobus.entity;

    import java.time.LocalDateTime;

    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;

    @Entity
    public class Trip {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "kalkis_terminal_id")
        private Terminal kalkisTerminali;

        @ManyToOne
        @JoinColumn(name = "varis_terminal_id")
        private Terminal varisTerminali;

        @ManyToOne
        @JoinColumn(name = "bus_id")
        private Bus otobus;

        private LocalDateTime kalkisSaati;
        private double fiyat;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Terminal getKalkisTerminali() { return kalkisTerminali; }
        public void setKalkisTerminali(Terminal kalkisTerminali) { this.kalkisTerminali = kalkisTerminali; }

        public Terminal getVarisTerminali() { return varisTerminali; }
        public void setVarisTerminali(Terminal varisTerminali) { this.varisTerminali = varisTerminali; }

        public Bus getOtobus() { return otobus; }
        public void setOtobus(Bus otobus) { this.otobus = otobus; }

        public LocalDateTime getKalkisSaati() { return kalkisSaati; }
        public void setKalkisSaati(LocalDateTime kalkisSaati) { this.kalkisSaati = kalkisSaati; }

        public double getFiyat() { return fiyat; }
        public void setFiyat(double fiyat) { this.fiyat = fiyat; }
    }