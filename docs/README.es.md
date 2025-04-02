<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">Una herramienta BI de código abierto simple y fácil de usar</h3>
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
## ¿Qué es DataEase?

DataEase es una herramienta de BI de código abierto diseñada para ayudar a los usuarios a analizar datos rápidamente y obtener información empresarial, lo que les permite mejorar y optimizar sus operaciones. Soporta una amplia gama de fuentes de datos, permitiendo a los usuarios crear gráficos con una interfaz simple de arrastrar y soltar y compartirlos sin esfuerzo.

**Ventajas de DataEase:**

-   Código Abierto: Sin barreras, adquisición e instalación rápida en línea, actualizaciones mensuales.
-   Fácil de Usar: Sencillo de utilizar; el análisis se puede completar con simples clics del ratón y acciones de arrastrar y soltar.
-   Versátil: Soporta instalación multiplataforma y opciones de integración diversificadas.
-   Compartición Segura: Ofrece varios métodos de compartición de datos mientras garantiza la seguridad de los datos.

** Fuentes de Datos Soportadas:**

- Bases de Datos OLTP: MySQL, Oracle, SQL Server, PostgreSQL, MariaDB, Db2, TiDB, MongoDB-BI, etc.
- Bases de Datos OLAP: ClickHouse, Apache Doris, Apache Impala, StarRocks, etc.
- Almacenes de Datos/Lagos de Datos: Amazon RedShift, etc.
- Archivos de Datos: Excel, CSV, etc.
- Fuentes de Datos API.

## Inicio Rápido

```
# Prepara un servidor Linux con al menos 2 CPUs y 4GB de RAM, luego ejecuta el siguiente script de instalación con un solo clic como usuario root:
curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash
# Nombre de usuario: admin
# Contraseña: DataEase@123456
```

## Pila Tecnológica

-   Frontend: [Vue.js](https://vuejs.org/), [Element](https://element.eleme.cn/)
-   Biblioteca de Visualización: [AntV](https://antv.vision/zh)
-   Backend: [Spring Boot](https://spring.io/projects/spring-boot)
-   Base de Datos: [MySQL](https://www.mysql.com/)
-   Procesamiento de Datos: [Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
-   Infraestructura: [Docker](https://www.docker.com/)

## Seguridad

Si descubres algún problema de seguridad, por favor contáctanos a través de: wei@fit2cloud.com.

## Licencia

Copyright (c) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/), Todos los derechos reservados.

Licenciado bajo la Licencia Pública General GNU versión 3 (GPLv3) (la "Licencia"); no puedes usar este archivo excepto en cumplimiento con la Licencia. Puedes obtener una copia de la Licencia en

<https://www.gnu.org/licenses/gpl-3.0.html>

A menos que lo exija la ley aplicable o se acuerde por escrito, el software distribuido bajo la Licencia se distribuye en una base "TAL CUAL", SIN GARANTÍAS O CONDICIONES DE NINGÚN TIPO, ya sean expresas o implícitas. Consulta la Licencia para el lenguaje específico que rige los permisos y limitaciones bajo la Licencia.
