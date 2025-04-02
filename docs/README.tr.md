<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">Basit ve kullanımı kolay açık kaynaklı BI aracı</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="GitHub Stars"></a>
  <a href="https://github.com/dataease/dataease/releases"><img src="https://img.shields.io/github/v/release/dataease/dataease" alt="GitHub release"></a>
</p>
<p align="center">
  <a href="/README.md"><img alt="中文(简体)" src="https://img.shields.io/badge/中文(简体)-d9d9d9"></a>
  <a href="/docs/README.en.md"><img alt="English" src="https://img.shields.io/badge/English-d9d9d9"></a>
  <a href="/docs/README.zh-Hant.md"><img alt="中文(繁體)" src="https://img.shields.io/badge/中文(繁體)-d9d9d9"></a>
  <a href="/docs/README.ja.md"><img alt="日本語" src="https://img.shields.io/badge/日本語-d9d9d9"></a>
  <a href="/docs/README.pt-br.md"><img alt="Português (Brasil)" src="https://img.shields.io/badge/Português (Brasil)-d9d9d9"></a>
  <a href="/docs/README.ar.md"><img alt="العربية" src="https://img.shields.io/badge/العربية-d9d9d9"></a>
  <a href="/docs/README.de.md"><img alt="Deutsch" src="https://img.shields.io/badge/Deutsch-d9d9d9"></a>
  <a href="/docs/README.es.md"><img alt="Español" src="https://img.shields.io/badge/Español-d9d9d9"></a>
  <a href="/docs/README.fr.md"><img alt="français" src="https://img.shields.io/badge/français-d9d9d9"></a>
  <a href="/docs/README.ko.md"><img alt="한국어" src="https://img.shields.io/badge/한국어-d9d9d9"></a>
  <a href="/docs/README.id.md"><img alt="Bahasa Indonesia" src="https://img.shields.io/badge/Bahasa Indonesia-d9d9d9"></a>
  <a href="/docs/README.tr.md"><img alt="Türkçe" src="https://img.shields.io/badge/Türkçe-d9d9d9"></a>
</p>

------------------------------
## DataEase nedir?

DataEase, kullanıcıların hızlı bir şekilde veri analizlerini yapmalarını ve iş içi bilgileri elde etmelerini sağlayarak işlemlerini geliştirmeleri ve optimize etmeleri için tasarlanmış açık kaynaklı bir BI aracıdır. Çeşitli veri kaynaklarını destekler ve kullanıcıların basit bir sürükle-bırak arayüzü ile grafikler oluşturmalarını ve bunları kolayca paylaşmalarını sağlar.

**DataEase'in Avantajları:**

-   Açık Kaynaklı: Sıfır engel, hızlı çevrimiçi edinme ve yükleme, aylık güncellemeler.
-   Kullanıcı Dostu: Kolay kullanılır; analizler, basit fare tıklamaları ve sürükle-bırak işlemleri ile tamamlanabilir.
-   Çoklu Amaçlı: Çoklu platform yüklemeyi ve çeşitli gömme seçeneklerini destekler.
-   Güvenli Paylaşım: Veri güvenliğini sağlayarak çeşitli veri paylaşım yöntemleri sunar.

**Desteklenen Veri Kaynakları:**

-   OLTP Veri Tabanları: MySQL, Oracle, SQL Server, PostgreSQL, MariaDB, Db2, TiDB, MongoDB-BI vb.
-   OLAP Veri Tabanları: ClickHouse, Apache Doris, Apache Impala, StarRocks vb.
-   Veri Ambarları/Veri Gölleri: Amazon RedShift vb.
-   Veri Dosyaları: Excel, CSV vb.
-   API Veri Kaynakları.

## Hızlı Başlangıç

```
# En az 2 CPU ve 4GB RAM ile bir Linux sunucusu hazırlayın ve ardından root kullanıcısı olarak aşağıdaki tek tuşlu yükleme komut dosyasını çalıştırın:

curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash

# Kullanıcı Adı: admin
# Şifre: DataEase@123456
```

## Teknoloji Yığını

-   Ön Kısmı: [Vue.js](https://vuejs.org/), [Element](https://element.eleme.cn/)
-   Görselleştirme Kütüphanesi: [AntV](https://antv.vision/zh)
-   Arka Kısmı: [Spring Boot](https://spring.io/projects/spring-boot)
-   Veri Tabanı: [MySQL](https://www.mysql.com/)
-   Veri İşleme: [Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
-   Altyapı: [Docker](https://www.docker.com/)

## Güvenlik

Eğer herhangi bir güvenlik sorunu keşfedin, lütfen bize wei@fit2cloud.com üzerinden ulaşın.

## Lisans

Copyright (c) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/), Tüm hakları saklıdır.

GNU Genel Kamu Lisansı sürüm 3 (GPLv3) (aşağıda "Lisans" olarak adlandırılır) altında lisanslanmıştır; bu dosyayı Lisans'a uygunlukla kullanmadıkça kullanamazsınız. Lisansın bir kopyasını aşağıdaki adresten edinebilirsiniz

<https://www.gnu.org/licenses/gpl-3.0.html>

Uygulanabilir yasa tarafından gerekli olmadıkça veya yazılı olarak kabul edilmedikçe, Lisans altında dağıtılan yazılım "OLARAK" temsil edildiği gibi dağıtılır, hiçbir tür garanti veya koşul olmaksızın, açıkça veya dolaylı olarak. Lisans altında izinleri ve kısıtlamaları düzenleyen spesifik dil için Lisansı inceleyin.
