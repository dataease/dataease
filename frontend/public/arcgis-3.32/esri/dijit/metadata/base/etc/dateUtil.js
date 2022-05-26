// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/base/etc/dateUtil","dojo/_base/lang dojo/_base/array dojo/has dojo/date/locale dojo/date dojo/date/stamp ../../../../kernel".split(" "),function(d,b,e,c,g,h,f){b={formatDate:function(a){return c.format(a,{datePattern:"yyyy-MM-dd",selector:"date"})},formatDateTime:function(a){var b=c.format(a,{datePattern:"yyyy-MM-dd",selector:"date"}),d=c.format(a,{timePattern:"HH:mm:ss.SSS",selector:"time"});a=c.format(a,{timePattern:"ZZZZ",selector:"time"});a=a.replace("GMT","");return b+
"T"+d+a}};e("extend-esri")&&d.setObject("dijit.metadata.base.etc.dateUtil",b,f);return b});