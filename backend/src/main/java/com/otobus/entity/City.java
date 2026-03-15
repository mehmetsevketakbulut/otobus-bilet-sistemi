package com.otobus.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Bir şehrin birden fazla otogarı (Terminali) olabilir.
    // Şimdilik bunu yoruma alıyorum, Terminal sınıfını oluşturunca açacağız.
    // @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    // private List<Terminal> terminals;

    // Getter ve Setter metotları (Kodu temiz tutmak için şimdilik manuel yazıyoruz,
    // ileride Lombok kullanıyorsanız bunları silebiliriz)

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
}