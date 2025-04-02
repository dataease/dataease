<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">A simple and easy-to-use open-source BI (Business Intelligence) tool</h3>
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

## What is DataEase?

DataEase is an open-source BI tool designed to help users quickly analyze data and gain business insights, enabling them to improve and optimize their operations. It supports a wide range of data sources, allowing users to create charts with a simple drag-and-drop interface and share them effortlessly.

**Advantages of DataEase:**

-   Open Source: Zero barriers, fast online acquisition and installation, monthly updates.
-   User-Friendly: Easy to use; analysis can be completed with simple mouse clicks and drag-and-drop actions.
-   Versatile: Supports multi-platform installation and diversified embedding options.
-   Secure Sharing: Offers various data-sharing methods while ensuring data security.

**Supported Data Sources:**

-   OLTP Databases: MySQL, Oracle, SQL Server, PostgreSQL, MariaDB, Db2, TiDB, MongoDB-BI, etc.
-   OLAP Databases: ClickHouse, Apache Doris, Apache Impala, StarRocks, etc.
-   Data Warehouses/Data Lakes: Amazon RedShift, etc.
-   Data Files: Excel, CSV, etc.
-   API Data Sources.

## Quick Start

```
# Prepare a Linux server with at least 2 CPUs and 4GB of RAM, then run the following one-click installation script as the root user:

curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash

# Username: admin
# Password: DataEase@123456
```

## Technology Stack

-   Frontend: [Vue.js](https://vuejs.org/), [Element](https://element.eleme.cn/)
-   Visualization Library: [AntV](https://antv.vision/zh)
-   Backend: [Spring Boot](https://spring.io/projects/spring-boot)
-   Database: [MySQL](https://www.mysql.com/)
-   Data Processing: [Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
-   Infrastructure: [Docker](https://www.docker.com/)

## Security

If you discover any security issues, please contact us through: wei@fit2cloud.com.

## License

Copyright (c) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
