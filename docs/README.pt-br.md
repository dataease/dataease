<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">Uma ferramenta BI de código aberto simples e fácil de usar</h3>
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

## O que é DataEase?

DataEase é uma ferramenta de BI de código aberto projetada para ajudar os usuários a analisar rapidamente dados e obter insights de negócios, permitindo-lhes melhorar e otimizar suas operações. Ele suporta uma ampla gama de fontes de dados, permitindo aos usuários criar gráficos com uma interface simples de arrastar e soltar e compartilhá-los sem esforço.

**Vantagens do DataEase:**

- Open Source: Zero barreiras, rápida aquisição e instalação on-line, atualizações mensais.
- Fácil de usar: Fácil de usar; A análise pode ser concluída com simples cliques do mouse e ações de arrastar e soltar.
- Versátil: Suporta instalação multi-plataforma e opções de incorporação diversificadas.
- Compartilhamento seguro: Oferece vários métodos de compartilhamento de dados, garantindo a segurança dos dados.

**Fontes de Dados Suportadas:**

- Bancos de dados OLTP: MySQL, Oracle, SQL Server, PostgreSQL, MariaDB, Db2, TiDB, MongoDB-BI, etc.
- Bancos de dados OLAP: ClickHouse, Apache Doris, Apache Impala, StarRocks, etc.
- Data Warehouses/Data Lakes: Amazon RedShift, etc.
- Arquivos de dados: Excel, CSV, etc.
- Fontes de Dados API.

## Início Rápido

```
# Prepare um servidor Linux com pelo menos 2 CPUs e 4GB de RAM e execute o seguinte script de instalação com um clique como usuário root:

curl - sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash

# Nome de utilizador: admin
# Senha: DataEase@123456
```

## Tecnologia Stack

- Frontend: [Vue.js](https://vuejs.org/), [Elemento](https://element.eleme.cn/)
- Biblioteca de Visualização: [AntV](https://antv.vision/zh)
- Infra- Estrutura: [Spring Boot](https://spring.io/projects/spring-boot)
- Base de dados: [MySQL](https://www.mysql.com/)
- Processamento de dados: [Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
- Infra-estrutura: [Docker](https://www.docker.com/)

## Segurança

Se você descobrir algum problema de segurança, entre em contato conosco através de: wei@fit2cloud.com.

## Licença

Copyright (c) 2014- 2024 [FIT2CLOUD](https://fit2cloud.com/), Todos os direitos reservados.

Licenciado sob a Licença Pública Geral GNU versão 3 (GPLv3) (a "Licença"); você não pode usar este arquivo exceto em conformidade com a Licença. Você pode obter uma cópia da Licença em

<https://www.gnu.org/licenses/gpl-3.0.html>

A menos que exigido pela lei aplicável ou acordado por escrito, o software distribuído sob a Licença é distribuído "COMO ESTÁ", SEM GARANTIAS OU CONDIÇÕES DE QUALQUER TIPO, expressas ou implícitas. Consulte a Licença para o idioma específico que rege permissões e limitações sob a Licença.
