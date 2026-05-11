# İLERİ WEB UYGULAMALARI DERSİ DÖNEM PROJESİ
# GÖREV PAYLAŞIM RAPORU

---

## PROJE BİLGİLERİ

| Bilgi | Detay |
|-------|-------|
| **Proje Adı** | OtoBilet — Otobüs Bilet Rezervasyon Sistemi |
| **Ders** | İleri Web Uygulamaları |
| **Teknoloji** | Java Spring Boot (Backend) + HTML/CSS/JS (Frontend) |
| **Mimari** | MVC (Model–View–Controller) |
| **Veritabanı** | MySQL |
| **GitHub** | https://github.com/mehmetsevketakbulut/otobus-bilet-sistemi |

---

## GRUP ÜYELERİ

| # | Ad Soyad | GitHub Kullanıcı Adı | E-posta |
|---|----------|---------------------|---------|
| 1 | İsmail Göz | ismail-gz | ismailgz1110@gmail.com |
| 2 | Arda Yılmaz | — | ard1ylmaz0@gmail.com |
| 3 | Mehmet Şevket Akbulut | mehmetsevketakbulut | akbulutmehmetsevket3@gmail.com |

---

## GÖREV PAYLAŞIMI

### 1. İsmail Göz — Backend Altyapı & Güvenlik & DevOps

**Sorumluluk Alanları:**
- Lokasyon yönetimi (City, Terminal, Bus) için Entity, Repository, Service ve Controller katmanlarının geliştirilmesi
- Segment bazlı biletleme mantığının uygulanması (TripStop, kısmi güzergah biletleme)
- Misafir ödeme sistemi entegrasyonu
- Hata yönetimi (Exception handling) altyapısı
- Brute-force koruması (LoginAttempt entity ve kontrol mekanizması)
- Audit log sistemi (AuditLog entity + AuditLogService)
- E-posta doğrulama altyapısı (EmailService)
- "Beni Hatırla" (Remember Me) özelliğinin backend entegrasyonu
- AWS Docker deployment yapılandırması (docker-compose.yml, Dockerfile)
- Firma panelinde dinamik ara durak ekleme sistemi
- Gece yarısı geçiş hesaplama algoritması (overnight trip support)
- Admin paneline sefer onay/red mekanizmasının eklenmesi

**İlgili Commit'ler:**
- `76db4da` — Dinamik ara durak sistemi, gece yarısı geçiş hesaplaması ve admin sefer onay paneli
- `a273b1a` — "Beni Hatırla" (Remember Me) fonksiyonelliği
- `2a7c7bd` — Brute-force koruması, audit logları, email doğrulama ve AWS Docker deployment
- `c488866` — Segment bazlı biletleme, misafir ödeme sistemi ve hata yönetimi
- `29e6b06` — City, Terminal ve Bus için REST API Controller sınıfları
- `7e99190` — City, Terminal ve Bus için Entity, Repository ve Service katmanları

---

### 2. Arda Yılmaz — Authentication & Frontend & Entegrasyonlar

**Sorumluluk Alanları:**
- JWT (JSON Web Token) tabanlı kimlik doğrulama altyapısının geliştirilmesi
- SecurityConfig yapılandırması (Spring Security + JWT filter)
- Kullanıcı kayıt ve giriş sisteminin uygulanması
- Şifre sıfırlama mekanizması (PasswordResetToken entity + email ile token gönderimi)
- Sefer oluşturma ve bilet satış mantığının geliştirilmesi
- Çift koltuk rezervasyon engelleme (pessimistic lock)
- E-posta doğrulama frontend entegrasyonu
- Profil sayfası ve "Biletlerim" bölümünün oluşturulması
- UI/UX iyileştirmeleri (arayüz tasarımı, responsive düzenlemeler)
- PNR duplikasyon hatasının giderilmesi
- Zaman sıralaması (time sorting) düzeltmeleri
- Aktif/Pasif firma kontrol sistemi
- Audit log admin paneli görüntüleme iyileştirmeleri

**İlgili Commit'ler:**
- `f3c484b` — UI iyileştirmeleri, PNR duplikasyon düzeltmesi, zaman sıralaması ve email entegrasyonu
- `52173284` — Şifre sıfırlama iyileştirmeleri
- `99d5ce7` — Aktif/Pasif kontrol, şifre sıfırlama formu ve audit log iyileştirmesi
- `23ba7c6` — Email doğrulama, bilet güvenliği ve profil biletlerim
- `14be763` — Auth sistemi, JWT altyapısı ve eksik servisler
- `e463883` — Sefer oluşturma, bilet satışı ve çift koltuk rezervasyon engelleme

---

### 3. Mehmet Şevket Akbulut — Proje Yönetimi & Veritabanı & Cloud

**Sorumluluk Alanları:**
- Projenin başlangıç iskeletinin oluşturulması (Spring Boot, Maven, pom.xml)
- GitHub repository yönetimi (branch stratejisi, pull request yönetimi)
- Veritabanı şeması tasarımı ve ilişkisel tablo yapısının planlanması
- 15 tabloluk veritabanı mimarisinin modellenmesi
- Bilet sistemi güncellemeleri ve test senaryoları
- AWS bulut altyapısının kurulumu ve deployment işlemleri
- readme.txt ve deployment raporunun hazırlanması
- Pull request'lerin review edilmesi ve merge işlemleri

**İlgili Commit'ler:**
- `993163e` — Bilet sistemi güncellemeleri
- `fc2377f` — Feature branch merge (sefer ve biletleme)
- `1dd6e41` — Feature branch merge (lokasyon ve filo)
- `342d838` — Proje iskeleti oluşturma

---

## VERİTABANI YAPISI (15 İlişkili Tablo)

| # | Tablo | Açıklama |
|---|-------|----------|
| 1 | users | Kullanıcı bilgileri (ad, email, şifre hash, rol, email doğrulama) |
| 2 | companies | Firma bilgileri (ad, aktif/pasif, sahip) |
| 3 | buses | Otobüs bilgileri (plaka, kapasite, firma ilişkisi) |
| 4 | cities | Şehir tanımları |
| 5 | terminals | Otogar bilgileri (şehir ilişkisi) |
| 6 | trips | Sefer bilgileri (kalkış/varış terminali, otobüs, fiyat, onay durumu) |
| 7 | trip_stops | Sefer durakları (sıra, saat, kalkıştan itibaren fiyat) |
| 8 | tickets | Bilet kayıtları (yolcu, sefer, koltuk, segment) |
| 9 | payments | Ödeme kayıtları (tutar, PNR, yöntem, durum) |
| 10 | reviews | Kullanıcı değerlendirmeleri (puan, yorum) |
| 11 | notifications | Bildirimler (kullanıcı, mesaj, okunma durumu) |
| 12 | audit_logs | İşlem logları (kullanıcı, aksiyon, entity, detay) |
| 13 | login_attempts | Giriş denemeleri (IP, email, başarı durumu, zaman) |
| 14 | password_reset_tokens | Şifre sıfırlama tokenları (token, son kullanma, kullanılma) |
| 15 | roles | Kullanıcı rolleri (ADMIN, COMPANY, USER) |

---

## GÜVENLİK ÖNLEMLERİ

| Önlem | Uygulama |
|-------|----------|
| SQL Injection | Spring Data JPA + Prepared Statements (ORM tabanlı) |
| XSS | Spring Security HTTP headers + Content Security Policy |
| CSRF | JWT Bearer Token kullanıldığı için cookie-based CSRF saldırılarına karşı koruma gereksiz (dokümante edilmiştir) |
| Şifre Hashleme | BCrypt algoritması ile güvenli hash |
| Brute-Force | 15 dakikada 5+ başarısız giriş → hesap kilidi |
| Yetkisiz Erişim | JWT token + Rol bazlı endpoint koruması (SecurityConfig) |
| Oturum Yönetimi | JWT expiration + "Beni Hatırla" ile uzatılmış süre |
| İşlem Logları | Tüm kritik işlemler AuditLog tablosuna kaydedilir |

---

## TEKNOLOJİ YIĞINI

- **Backend:** Java 25, Spring Boot 3.5, Spring Security, Spring Data JPA
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla), TailwindCSS CDN
- **Veritabanı:** MySQL
- **Authentication:** JWT (JSON Web Token)
- **E-posta:** Gmail SMTP (JavaMailSender)
- **Containerization:** Docker, Docker Compose
- **Deployment:** AWS EC2 (Free Tier)
- **Versiyon Kontrol:** Git, GitHub

---

## HAFTALIK İLERLEME ÖZETİ

| Hafta | Tarih | Yapılan İş |
|-------|-------|------------|
| 1 | 14 Mart 2026 | Proje iskeleti oluşturuldu, GitHub repo açıldı |
| 2 | 15 Mart 2026 | City/Terminal/Bus entity'leri ve API'leri, sefer ve biletleme mantığı |
| 3-6 | Mart–Nisan 2026 | Geliştirme ve test süreci |
| 7 | 2 Mayıs 2026 | Auth sistemi (JWT), login/register altyapısı |
| 8 | 3 Mayıs 2026 | Bilet sistemi güncellemeleri |
| 9 | 4 Mayıs 2026 | Segment bazlı biletleme, misafir ödeme, hata yönetimi |
| 10 | 11 Mayıs 2026 | Güvenlik (brute-force, audit log, email doğrulama, Docker) |
| 10 | 11 Mayıs 2026 | Beni Hatırla, şifre sıfırlama, UI iyileştirmeleri |
| 10 | 11 Mayıs 2026 | Dinamik durak sistemi, gece geçişi, admin sefer onayı |
| 11 | 11 Mayıs 2026 | Admin CRUD tamamlama, firma paneli, son düzeltmeler |

---

*Bu rapor, proje geliştirme sürecindeki görev dağılımını ve her grup üyesinin katkılarını GitHub commit geçmişine dayanarak belgelemektedir.*
