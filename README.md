<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">人人可用的开源数据可视化分析工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease/releases/latest"><img src="https://img.shields.io/github/v/release/dataease/dataease" alt="Latest release"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="Stars"></a>
  <a href="https://app.fossa.com/projects/git%2Bgithub.com%2F1dataease%2Fdataease?ref=badge_shield"><img src="https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdataease%2Fdataease.svg?type=shield" alt="FOSSA Status"></a>  
</p>
<hr/>

## 什么是 DataEase？

DataEase 是开源的数据可视化分析工具，帮助用户快速分析数据并洞察业务趋势，从而实现业务的改进与优化。DataEase 支持丰富的数据源连接，能够通过拖拉拽方式快速制作图表，并可以方便的与他人分享。

![de-architecture](https://dataease.io/images/screenshot/de-chart-new.jpg)

**DataEase 的功能包括：**

-   图表展示：支持 PC 端、移动端、大屏;
-   图表制作：支持丰富的图表类型(Apache ECharts / AntV)、支持拖拉拽方式快速制作仪表板;
-   数据引擎：支持直连模式、本地模式(基于 Apache Doris / Kettle 实现);
-   数据连接：支持数据仓库/数据湖、OLAP 数据库、OLTP 数据库、Excel 数据文件、API 等各种数据源。

## DataEase 的优势

-   开源开放：零门槛，线上快速获取和安装；快速获取用户反馈、按月发布新版本；
-   简单易用：极易上手，通过鼠标点击和拖拽即可完成分析；
-   秒级响应：集成 Apache Doris，超大数据量下秒级查询返回延时；
-   安全分享：支持多种数据分享方式，确保数据安全。

## DataEase 支持的数据源

<p align="center">
  <img src="https://dataease.io/images/dataSource/excel.jpg" alt="excel" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mysql.png" alt="mysql" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/oracle.jpg" alt="oracle" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/sqlservel.jpg" alt="sqlserver" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mariadb.jpg" alt="mariadb" border="0" width="155" height="107"/>  
  <img src="https://dataease.io/images/dataSource/elasticsearch.jpg" alt="elasticsearch" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/clickhouse.jpg" alt="clickhouse" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/doris.jpg" alt="doris" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/mongodb.jpg" alt="mongodb" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/redshift.jpg" alt="redshift" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/hive.png" alt="hive" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/DB2.jpg" alt="DB2" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/API.jpg" alt="API" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/TiDB.jpg" alt="TiDB" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/StarRocks.jpg" alt="StarRocks" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/PrestoDB.jpg" alt="PrestoDB" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/dm.jpg" alt="dm" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/kingbase.jpg" alt="KingBase" border="0" width="155" height="107"/>
  <img src="https://dataease.io/images/dataSource/kylin.jpg" alt="Kylin" border="0" width="180" height="107"/>
</p>

> 更多数据源支持持续增加中...

## DataEase 模板市场

-   [模板市场](https://dataease.io/templates/)

![模板市场](https://dataease.io/images/templates/templates.gif)

> 更多优质模板持续增加中...

## 快速开始

**在线体验**

-   环境地址：<https://dataease.fit2cloud.com/>
-   用户名：demo
-   密码：dataease

**一键安装**

仅需两步快速安装 DataEase：

1. 准备一台不小于 8 G内存的 64位 Linux 主机；
2. 以 root 用户执行如下命令一键安装 DataEase。

```sh
curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start.sh | bash
```

**学习资料**

-   [在线文档](https://dataease.io/docs/)
-   [社区论坛](https://bbs.fit2cloud.com/c/de/6)

**加入微信交流群**

<img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/wechat-helper.png" width="156" height="156"/>

## DataEase 的技术栈

-   前端：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)
-   图库：[Apache ECharts](https://github.com/apache/echarts)、[AntV](https://antv.vision/zh)
-   后端：[Spring Boot](https://spring.io/projects/spring-boot)
-   中间件：[MySQL](https://www.mysql.com/)
-   数据处理：[Kettle](https://github.com/pentaho/pentaho-kettle)、[Apache Doris](https://github.com/apache/incubator-doris/)
-   基础设施：[Docker](https://www.docker.com/)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=dataease/dataease&type=Date)](https://star-history.com/#dataease/dataease&Date)

## FOSSA Status

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdataease%2Fdataease.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fdataease%2Fdataease?ref=badge_large)


## License

Copyright (c) 2014-2023 [FIT2CLOUD 飞致云](https://fit2cloud.com/), All rights reserved.

Licensed under The GNU General Public License version 3 (GPLv3)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-3.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
