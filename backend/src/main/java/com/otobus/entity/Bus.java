package com.otobus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "buses")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aynı plakanın iki kez eklenmesini veritabanı seviyesinde engelliyoruz
    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private int seatCapacity;

    // KURAL: Otobüs ile Firma (ManyToOne) bağlantısı
    // Birçok otogar (Many) tek bir firmaya (One) ait olabilir.
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // --- Getter ve Setter Metotları ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}