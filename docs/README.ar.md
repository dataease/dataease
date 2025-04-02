<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center"> أداة استخبارات الأعمال مفتوحة المصدر بسيطة وسهلة الاستخدام. </h3>
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

## ما هو DataEase؟

<div dir="rtl">
DataEase هو أداة تحليل بيانات مفتوحة المصدر مصممة لمساعدة المستخدمين على تحليل البيانات بسرعة واحتساب رؤى عملية، مما يتيح لهم تحسين وتحسين عملياتهم. كما تدعم العديد من مصادر البيانات، مما يسمح للمستخدمين بإنشاء مخططات من خلال واجهة سحب ووضع بسيطة وشاركها بسهولة. 

**مزايا DataEase:**

-   مفتوح المصدر: بلا حواجز، استلام وتثبيت سريع عبر الإنترنت، تحديثات شهريّة.
-   مريح للاستخدام: من السهل الاستخدام؛ يمكن إكمال التحليل ببضعة نقرات وميقاتات بسيطة بالماوس.
-   متعدد الاستخدامات: يدعم تثبيت متعدد الأنصتات وخيارات تضمين متنوعة.
-   مشاركة آمنة: توفر طرقًا مختلفة لمشاركة البيانات مع التأكد من أمن البيانات.

**مصادر البيانات المدعومة:**

-   قواعد OLTP: MySQL، Oracle، SQL Server، PostgreSQL، MariaDB، Db2، TiDB، MongoDB-BI، وما إلى ذلك.
-   قواعد OLAP: ClickHouse، Apache Doris، Apache Impala، StarRocks، وما إلى ذلك.
-   مخازن البيانات/بحيرات البيانات: Amazon RedShift، وما إلى ذلك.
-  ملفات البيانات: Excel، CSV، وما إلى ذلك.
-   مصادر البيانات عبر واجهة برمجة التطبيقات (API).

## التشغيل السريع

```
# أعد تجهيز خادم Linux بحد أدنى معالجين مركزيين وذاكرة 4 غيغابايت، ثم قم بتشغيل سكربت تثبيت بضغطة واحدة التالي كوظيفة مستخدم جذر.
curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash
# اسم المستخدم: admin
# كلمة المرور: DataEase@123456
```

## مكدس التقنية

-   الواجهة الأمامية: [Vue.js](https://vuejs.org/), [Element](https://element.eleme.cn/)
-   مكتبة تصور البيانات.: [AntV](https://antv.vision/zh)
-   الخلفية: [Spring Boot](https://spring.io/projects/spring-boot)
-   قاعدة البيانات: [MySQL](https://www.mysql.com/)
-   معالجة البيانات: [Apache Calcite](https://github.com/apache/calcite/), [Apache SeaTunnel](https://github.com/apache/seatunnel)
-   بنية تحتية: [Docker](https://www.docker.com/)

## الحماية

إذا اكتشفت أي مشاكل أمنية، فالرجاء الاتصال بنا عبر: wei@fit2cloud.com. 

## رخصة

حقوق النشر (م) 2014-2024 [FIT2CLOUD](https://fit2cloud.com/)، جميع الحقوق محفوظة. 

رخصة بموجب الرخصة العامة العامة GNU النسخة 3 (GPLv3) (ما يسمى "الرخصة"); ربما لا تستطيع استخدام هذا الملف إلا في توافق مع الرخصة. يمكنك الحصول على نسخة من الرخصة من خلال الرابط التالي: 

<https://www.gnu.org/licenses/gpl-3.0.html> 

ما لم يكن ذلك مطلوبًا بموجب القانون المعمول به أو وافق عليه كتابيًا، فإن البرنامج الموزع بموجب الرخصة يتم توزيعه على أساس "كما هو"، بلا أي نوع من الضمانات أو الشروط، سواء كانت صريحة أو ضمنية. راجع الرخصة للحصول على اللغة المحددة التي تحكم الصلاحيات والقيود بموجب الرخصة. 

</div>
