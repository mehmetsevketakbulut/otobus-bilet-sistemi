package com.otobus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;

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

    @ManyToOne
    @JoinColumn(name = "from_stop_id")
    private TripStop fromStop;

    @ManyToOne
    @JoinColumn(name = "to_stop_id")
    private TripStop toStop;

    // Yeni alanlar: cinsiyet, TC No, kullanıcı ilişkisi
    @Column(length = 10)
    private String gender; // "male" veya "female"

    @Column(length = 11)
    private String tcNo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // --- Getter ve Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getYolcuAdSoyad() {
        return yolcuAdSoyad;
    }

    public void setYolcuAdSoyad(String yolcuAdSoyad) {
        this.yolcuAdSoyad = yolcuAdSoyad;
    }

    public int getKoltukNo() {
        return koltukNo;
    }

    public void setKoltukNo(int koltukNo) {
        this.koltukNo = koltukNo;
    }

    public TripStop getFromStop() {
        return fromStop;
    }

    public void setFromStop(TripStop fromStop) {
        this.fromStop = fromStop;
    }

    public TripStop getToStop() {
        return toStop;
    }

    public void setToStop(TripStop toStop) {
        this.toStop = toStop;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTcNo() {
        return tcNo;
    }

    public void setTcNo(String tcNo) {
        this.tcNo = tcNo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}