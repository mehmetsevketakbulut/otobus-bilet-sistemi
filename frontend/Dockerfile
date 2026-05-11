FROM nginx:alpine

# Nginx default sayfasını sil
RUN rm -rf /usr/share/nginx/html/*

# Tüm frontend dosyalarını nginx yayın dizinine kopyala
COPY . /usr/share/nginx/html/

# Özel Nginx konfigürasyonunu kopyala (API proxy yönlendirmeleri için)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
