package com.otobus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "terminals")
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // İŞTE KRİTİK NOKTA BURASI: ManyToOne İlişkisi
    // Birçok otogar (Many) tek bir şehre (One) bağlanabilir.
    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    // Getter ve Setter metotları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}