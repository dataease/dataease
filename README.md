<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">人人可用的开源数据可视化分析工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="Stars"></a>
  <a href="https://app.fossa.com/projects/git%2Bgithub.com%2F1dataease%2Fdataease?ref=badge_shield"><img src="https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdataease%2Fdataease.svg?type=shield" alt="FOSSA Status"></a>  
</p>

|说明|
|------------------|
|此分支为 DataEase v2 版本的开发分支。DataEase v2 正在快速迭代中，如是在生产环境部署 DataEase，建议使用 v1.18.* 的稳定版本。|
<hr/>

## 什么是 DataEase？

DataEase 是开源的数据可视化分析工具，帮助用户快速分析数据并洞察业务趋势，从而实现业务的改进与优化。DataEase 支持丰富的数据源连接，能够通过拖拉拽方式快速制作图表，并可以方便的与他人分享。

**DataEase 的功能包括：**

-   图表展示：支持 PC 端、移动端、大屏及嵌入式使用场景;
-   图表制作：支持丰富的图表类型、支持拖拉拽方式快速制作仪表板;
-   数据处理：基于 Apache Calcite，实现统一的 SQL 解析、验证、优化和执行;
-   数据连接：支持数据仓库/数据湖、OLAP 数据库、OLTP 数据库、Excel 数据文件、API 等各种数据源。

**DataEase 的优势：**

-   开源开放：零门槛，线上快速获取和安装；快速获取用户反馈、按月发布新版本；
-   简单易用：极易上手，通过鼠标点击和拖拽即可完成分析；
-   全场景支持：多平台支持、多种嵌入式方案支持；
-   安全分享：支持多种数据分享方式，确保数据安全。

**DataEase 支持的数据源：**

<p align="center">
  <img src="https://dataease.io/images/dataSource/excel.jpg" alt="excel" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mysql.png" alt="mysql" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/oracle.jpg" alt="oracle" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/sqlservel.jpg" alt="sqlserver" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mariadb.jpg" alt="mariadb" border="0" width="155" height="107"/>  
  <img src="https://dataease.io/images/dataSource/clickhouse.jpg" alt="clickhouse" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/doris.jpg" alt="doris" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mongodb.jpg" alt="mongodb" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/redshift.jpg" alt="redshift" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/DB2.jpg" alt="DB2" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/TiDB.jpg" alt="TiDB" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/StarRocks.jpg" alt="StarRocks" border="0" width="155" height="107"/>
</p>

**DataEase 的技术栈：**

-   前端：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)
-   图库：[AntV](https://antv.vision/zh)
-   后端：[Spring Boot](https://spring.io/projects/spring-boot)
-   中间件：[MySQL](https://www.mysql.com/)
-   数据处理：[Apache Calcite](https://github.com/apache/calcite/)
-   基础设施：[Docker](https://www.docker.com/)

## License

Copyright (c) 2014-2023 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
