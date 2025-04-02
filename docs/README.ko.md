<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">사용하기 쉬운 오픈소스 BI 도구 (Sayonghagi swiun open source BI dogu)</h3>
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

## DataEase 란?

DataEase는 사용자가 신속하게 데이터를 분석하고 비즈니스 통찰력을 얻을 수 있도록 설계된 오픈소스 BI 도구로, 운영을 개선하고 최적화할 수 있습니다.광범위한 데이터 원본을 지원하므로 사용자는 간단한 드래그 앤 드롭 인터페이스로 차트를 만들고 손쉽게 공유할 수 있습니다.

**DataEase의 장점:**

-   오픈 소스:장벽이 없는, 빠른 온라인 획득 및 설치, 월별 업데이트.
-   사용자 친화적인:사용하기 쉬운;간단한 마우스 클릭과 드래그 앤 드롭 동작으로 분석을 완료할 수 있습니다.
-   다용도:멀티 플랫폼 설치 및 다양한 임베딩 옵션을 지원합니다.
-   보안 공유:데이터 보안을 보장하면서 다양한 데이터 공유 방법을 제공합니다.

**지원되는 데이터 원본 Supported Data Sources:**

- OLTP 데이터베이스:MySQL, Oracle, SQL Server, PostgreSQL, MariaDB, Db2, TiDB, MongoDB-BI 등.
- OLAP 데이터베이스:ClickHouse, Apache Doris, Apache Impala, StarRocks 등.
- Data warehouse/Data Lakes:Amazon RedShift 등
- 데이터파일:Excel, CSV 등.
- API 데이터 원본.

## 빠 른 시작

```
# 최소 2개의 cpu와 4GB의 RAM이 있는 Linux 서버를 준비한 후 루트 사용자로 다음 원클릭 설치 스크립트를 실행합니다:

컬 -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | 파티

# 사용자 이름:관리
# 암호:DataEase@123456
```

## 기술 스택

- 프론트엔드:[Vue.js](https://vuejs.org/), [Element](https://element.eleme.cn/)
- 시각화 라이브러리:[AntV](https://antv.vision/zh)
- 백엔드:[스프링 부트](https://spring.io/projects/spring-boot)
- 데이터베이스:[MySQL](https://www.mysql.com/)
- 자료처리:[Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
- 인프라:[Docker](https://www.docker.com/)

## 보안

보안 문제를 발견하면 다음을 통해 문의하십시오:wei@fit2cloud.com.

## 라이선스

Copyright (c) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/), All rights reserved.

GNU 일반 공중 사용 허가서 버전 3 (GPLv3)에 따라 허가 ("라이선스");라이센스를 준수하는 경우를 제외하고이 파일을 사용할 수 없습니다.라이센스의 사본을 받을 수 있습니다

< https://www.gnu.org/licenses/gpl-3.0.html >

해당 법률에서 요구하거나 서면으로 동의하지 않는 한, 라이선스에 따라 배포되는 소프트웨어는 명시적 또는 묵시적으로 어떤 종류의 보증이나 조건도 없이"있는 그대로"배포됩니다.라이선스에서 사용 권한 및 제한 사항을 관리하는 특정 언어는 라이선스를 참조하십시오.
