# ===== OTOBÜS BİLET SİSTEMİ - AWS DEPLOYMENT VE KURULUM REHBERİ =====

# Bu dosya projenin AWS (veya herhangi bir VPS) üzerinde ayağa kaldırılması için gereken adımları içerir.

1. GEREKSİNİMLER
----------------
Hedef sunucuda (örneğin AWS EC2 Ubuntu makinesinde) aşağıdaki araçların kurulu olması gerekir:
- Docker
- Docker Compose
- Git (Projeyi sunucuya çekmek için)

2. SUNUCU KURULUM ADIMLARI
--------------------------
Adım 1: AWS Yönetim Konsolundan bir "Ubuntu 22.04 LTS" veya "Ubuntu 24.04 LTS" EC2 instance (t2.micro - Free Tier) başlatın.
Adım 2: Security Group ayarlarından şu portları dışarıya açın:
   - 22 (SSH)
   - 80 (HTTP - Frontend)
   - 8080 (Backend API)
   - 3306 (MySQL - Sadece kendi IP adresinize açmanız güvenlik için daha iyidir)

Adım 3: Sunucuya SSH ile bağlanın ve Docker'ı kurun:
   sudo apt update
   sudo apt install docker.io docker-compose -y
   sudo systemctl enable docker
   sudo systemctl start docker

Adım 4: Projeyi sunucuya kopyalayın (Git clone veya SCP ile).

Adım 5: Proje kök dizininde (docker-compose.yml dosyasının olduğu yerde) şu komutu çalıştırın:
   sudo docker-compose up -d --build

Bu komut:
- MySQL veritabanını ayağa kaldırır.
- Spring Boot backend'ini derler ve 8080 portunda çalıştırır.
- Nginx kullanarak Frontend'i 80 portunda yayınlar.

3. SİSTEMİN TEST EDİLMESİ
-------------------------
- Frontend Erişimi: Tarayıcınızdan http://<EC2-PUBLIC-IP>/ adresine gidin.
- Backend API Kontrolü: http://<EC2-PUBLIC-IP>:8080/api/trips/search adresine istek atarak kontrol edin.
- Admin Paneli: http://<EC2-PUBLIC-IP>/admin.html adresinden test edebilirsiniz.

4. GÜVENLİK NOTLARI (BİR SİBER GÜVENLİK UZMANI OLARAK ÖNERİLER)
---------------------------------------------------------------
- Veritabanı şifreleri "docker-compose.yml" içinde environment variable olarak ayarlanmıştır. Canlıya almadan önce bu şifreleri değiştirin.
- JWT Secret key'i karmaşık bir yapı ile güncelleyin.
- XSS ve Brute-Force korumaları Backend'de aktiftir. Güvenli bir bağlantı için AWS üzerinden bir Load Balancer (ALB) ekleyip SSL sertifikası (HTTPS) tanımlamanız şiddetle tavsiye edilir.

5. VERİTABANI İLK KURULUM (SEEDING)
-----------------------------------
Hibernate "update" modunda çalıştığı için tablolar otomatik oluşacaktır.
Yönetici hesabı için backend üzerinden /api/auth/register endpointine "ADMIN" rolüyle bir kayıt atmanız yeterlidir.
