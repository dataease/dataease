<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">誰でも使えるオープンソースBIツール</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0.html"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF" alt="License: GPL v3"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="GitHub Stars"></a>
  <a href="https://gitee.com/fit2cloud-feizhiyun/DataEase"><img src="https://gitee.com/fit2cloud-feizhiyun/DataEase/badge/star.svg?theme=gvp" alt="Gitee Stars"></a><br>
  [<a href="/README_JP.md">日本語</a>] | [<a href="/README.md">中文(简体)</a>] | [<a href="/README_EN.md">English</a>]
</p>

------------------------------

## DataEaseとは？

DataEaseは、ユーザーがデータを迅速に分析し、ビジネスの洞察を得るのを支援するオープンソースのBIツールです。これにより、ユーザーは業務の改善と最適化を実現できます。DataEaseは多様なデータソースをサポートし、ドラッグアンドドロップのインターフェースで簡単にチャートを作成し、他の人と簡単に共有できます。

**DataEaseの利点：**

-   オープンソース：障壁がなく、オンラインで迅速に取得およびインストールでき、毎月更新されます。
-   使いやすい：使いやすく、マウスクリックとドラッグアンドドロップで分析を完了できます。
-   多用途：マルチプラットフォームインストールと多様な埋め込みオプションをサポートします。
-   安全な共有：データのセキュリティを確保しながら、さまざまなデータ共有方法を提供します。

**サポートされているデータソース：**

-   OLTPデータベース：MySQL、Oracle、SQL Server、PostgreSQL、MariaDB、Db2、TiDB、MongoDB-BIなど。
-   OLAPデータベース：ClickHouse、Apache Doris、Apache Impala、StarRocksなど。
-   データウェアハウス/データレイク：Amazon RedShiftなど。
-   データファイル：Excel、CSVなど。
-   APIデータソース。

## クイックスタート

```
# 2CPU、4GB以上のRAMを備えたLinuxサーバーを準備し、rootユーザーとして以下のワンクリックインストールスクリプトを実行します：

curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash

# ユーザー名: admin
# パスワード: DataEase@123456
```

## 技術スタック

-   フロントエンド：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)
-   可視化ライブラリ：[AntV](https://antv.vision/zh)
-   バックエンド：[Spring Boot](https://spring.io/projects/spring-boot)
-   データベース：[MySQL](https://www.mysql.com/)
-   データ処理：[Apache Calcite](https://github.com/apache/calcite/)、[Apache SeaTunnel](https://github.com/apache/seatunnel)
-   インフラストラクチャ：[Docker](https://www.docker.com/)

## セキュリティ

セキュリティ上の問題を発見した場合は、以下のメールアドレスにご連絡ください：wei@fit2cloud.com。

## ライセンス

Copyright (c) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/), All rights reserved.

The GNU General Public License version 3 (GPLv3) に基づいてライセンスされています。ライセンスに従わない限り、このファイルを使用することはできません。ライセンスのコピーは以下のリンクから入手できます：

<https://www.gnu.org/licenses/gpl-3.0.html>

適用法または書面によって同意されない限り、ライセンスに基づいて配布されるソフトウェアは、「現状のまま」提供され、明示的または黙示的な保証はありません。ライセンスの下での権利と制限の詳細については、ライセンスを参照してください。
