package com.otobus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private String yolcuAdSoyad;
    private int koltukNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public String getYolcuAdSoyad() { return yolcuAdSoyad; }
    public void setYolcuAdSoyad(String yolcuAdSoyad) { this.yolcuAdSoyad = yolcuAdSoyad; }

    public int getKoltukNo() { return koltukNo; }
    public void setKoltukNo(int koltukNo) { this.koltukNo = koltukNo; }
}